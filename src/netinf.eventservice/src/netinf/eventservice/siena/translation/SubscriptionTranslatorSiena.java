/*
 * Copyright (C) 2009-2011 University of Paderborn, Computer Networks Group
 * (Full list of owners see http://www.netinf.org/about-2/license)
 * 
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:
 * 
 *     * Redistributions of source code must retain the above copyright notice,
 *       this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright notice,
 *       this list of conditions and the following disclaimer in the documentation and/or other materials provided with the distribution.
 *     * Neither the name of the University of Paderborn nor the names of its contributors may be used to endorse
 *       or promote products derived from this software without specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY
 * EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT
 * SHALL THE COPYRIGHT HOLDERS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package netinf.eventservice.siena.translation;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;

import netinf.common.exceptions.NetInfCheckedException;
import netinf.common.exceptions.NetInfUncheckedException;
import netinf.eventservice.siena.exceptions.SubscriptionInfiniteVariableRecursionException;
import netinf.eventservice.siena.exceptions.SubscriptionInvalidFilterException;
import netinf.eventservice.siena.exceptions.SubscriptionInvalidFilterOperatorException;
import netinf.eventservice.siena.exceptions.SubscriptionInvalidResultVariablesException;
import netinf.eventservice.siena.exceptions.SubscriptionInvalidSolutionModifierException;
import netinf.eventservice.siena.exceptions.SubscriptionInvalidTripleObjectException;
import netinf.eventservice.siena.exceptions.SubscriptionInvalidTriplePredicateException;
import netinf.eventservice.siena.exceptions.SubscriptionInvalidTripleSubjectException;
import netinf.eventservice.siena.exceptions.SubscriptionInvalidWhereClauseException;
import netinf.eventservice.siena.exceptions.SubscriptionQueryParseException;

import org.apache.log4j.Logger;

import siena.Filter;
import siena.Op;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.Node_Literal;
import com.hp.hpl.jena.graph.Node_URI;
import com.hp.hpl.jena.graph.Node_Variable;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.QueryParseException;
import com.hp.hpl.jena.sparql.expr.E_Bound;
import com.hp.hpl.jena.sparql.expr.E_LogicalNot;
import com.hp.hpl.jena.sparql.expr.Expr;
import com.hp.hpl.jena.sparql.expr.ExprFunction2;
import com.hp.hpl.jena.sparql.expr.ExprVar;
import com.hp.hpl.jena.sparql.expr.NodeValue;
import com.hp.hpl.jena.sparql.expr.nodevalue.NodeValueBoolean;
import com.hp.hpl.jena.sparql.expr.nodevalue.NodeValueDate;
import com.hp.hpl.jena.sparql.expr.nodevalue.NodeValueDateTime;
import com.hp.hpl.jena.sparql.expr.nodevalue.NodeValueDecimal;
import com.hp.hpl.jena.sparql.expr.nodevalue.NodeValueDouble;
import com.hp.hpl.jena.sparql.expr.nodevalue.NodeValueFloat;
import com.hp.hpl.jena.sparql.expr.nodevalue.NodeValueInteger;
import com.hp.hpl.jena.sparql.expr.nodevalue.NodeValueString;
import com.hp.hpl.jena.sparql.syntax.Element;
import com.hp.hpl.jena.sparql.syntax.ElementFilter;
import com.hp.hpl.jena.sparql.syntax.ElementGroup;
import com.hp.hpl.jena.sparql.syntax.ElementTriplesBlock;

/**
 * SubscriberSiena may have to translate multiple ESFSubscriptionRequest instances concurrently and thus cannot use object
 * variables to store information that is relevant while translating one particular ESFSubscriptionRequest (which leads to ugly
 * code). Hence, for each ESFSubscriptionRequest one instance of SubscriptionTranslatorSiena is instantiated which in turn _can_
 * make use of object variables.<br>
 * <br>
 * This class itself is not (intended to be) thread-safe! Create one instance per ESFSubscriptionRequest!
 * 
 * @author PG Augnet 2, University of Paderborn
 */
public class SubscriptionTranslatorSiena {
   private static final Logger LOG = Logger.getLogger(SubscriptionTranslatorSiena.class);

   private static final String SPARQL_EQ = "=";
   private static final String SPARQL_NE = "!=";
   private static final String SPARQL_LT = "<";
   private static final String SPARQL_GT = ">";
   private static final String SPARQL_LE = "<=";
   private static final String SPARQL_GE = ">=";

   private final String sparqlQuery;
   private Filter sienaFilter;
   private ArrayList<ElementFilter> filters;
   private ArrayList<Triple> triplesWithObjectLiteral;
   private Hashtable<String, Triple> triplesWithObjectVariable;

   /**
    * Constructor
    * 
    * @param sparqlQuery
    *           the SparQL query string
    */
   public SubscriptionTranslatorSiena(String sparqlQuery) {
      this.sparqlQuery = sparqlQuery;
   }

   /**
    * Returns a set of Siena Filters based on the SparQL query string
    * 
    * @return a set of Siena Filters
    * @throws NetInfCheckedException
    */
   public List<Filter> buildSienaFilters() throws NetInfCheckedException {
      LOG.trace(null);

      Query query = null;
      try {
         query = QueryFactory.create(this.sparqlQuery);
      } catch (QueryParseException qpe) {
         throw new SubscriptionQueryParseException(qpe.getMessage());
      }

      if (query.hasGroupBy() || query.hasOrderBy() || query.hasHaving() || query.hasOffset() || query.hasLimit()) {
         throw new SubscriptionInvalidSolutionModifierException("GROUP BY, ORDER BY, HAVING, "
               + "OFFSET and LIMIT are not supported!");
      }

      List<String> resultVars = query.getResultVars();
      if (resultVars.size() != 2 || !resultVars.get(0).equals(TranslationSiena.PREFIX_OLD)
            || !resultVars.get(1).equals(TranslationSiena.PREFIX_NEW)) {
         throw new SubscriptionInvalidResultVariablesException("The result variables have to be \"?"
               + TranslationSiena.PREFIX_OLD + " ?" + TranslationSiena.PREFIX_NEW + "\"!");
      }

      ElementGroup pattern = (ElementGroup) query.getQueryPattern();

      // Step 1: Make a distinction between 3 different kinds of WHERE clause components
      this.filters = new ArrayList<ElementFilter>();
      this.triplesWithObjectLiteral = new ArrayList<Triple>();
      this.triplesWithObjectVariable = new Hashtable<String, Triple>();

      for (Element element : pattern.getElements()) {
         if (element instanceof ElementTriplesBlock) {
            ElementTriplesBlock elementTriplesBlock = (ElementTriplesBlock) element;

            for (Triple triple : elementTriplesBlock.getPattern().getList()) {
               Node uncastedObject = triple.getObject();

               if (uncastedObject instanceof Node_Literal) {
                  // a) Triple with Literal Object
                  this.triplesWithObjectLiteral.add(triple);
               } else if (uncastedObject instanceof Node_Variable) {
                  // b) Triple with Variable Object
                  Node_Variable object = (Node_Variable) triple.getObject();
                  this.triplesWithObjectVariable.put(object.getName(), triple);
               } else {
                  throw new SubscriptionInvalidTripleObjectException(
                        "Triple Object is something else than a Variable or a Literal: " + uncastedObject);
               }
            }
         } else if (element instanceof ElementFilter) {
            // c) Filter
            this.filters.add((ElementFilter) element);
         } else {
            throw new SubscriptionInvalidWhereClauseException("WHERE clause consist of something else than Triples and Filters: "
                  + element);
         }
      }

      // Step 2a: Create a Siena constraint for each Triple with a literal Object
      this.sienaFilter = new Filter();

      for (Triple triple : this.triplesWithObjectLiteral) {
         addConstraintForTripleWithLiteralObject(triple);
      }

      // Step 2b: Create a Siena constraint for each Filter
      HashSet<String> boundedPaths = new HashSet<String>();
      HashSet<String> unboundedPaths = new HashSet<String>();

      for (ElementFilter filter : this.filters) {
         Expr expr = filter.getExpr();

         if (expr instanceof ExprFunction2) {
            addConstraintForBinaryFilter((ExprFunction2) expr);
         } else if (expr instanceof E_Bound) {
            addConstraintForBoundFilter(boundedPaths, unboundedPaths, (E_Bound) expr, TranslationSiena.PREFIX_NEW);
         } else if (expr instanceof E_LogicalNot && ((E_LogicalNot) expr).getArg() instanceof E_Bound) {
            E_Bound boundExpr = (E_Bound) ((E_LogicalNot) expr).getArg();
            addConstraintForBoundFilter(unboundedPaths, boundedPaths, boundExpr, TranslationSiena.PREFIX_OLD);
         } else {
            throw new SubscriptionInvalidFilterException("Unsupported Filter: " + expr);
         }
      }

      if (boundedPaths.size() != 0 || unboundedPaths.size() != 0) {
         throw new SubscriptionInvalidFilterException("Bound clauses in Filters have to occur in couples!");
      }

      LOG.debug(this.sienaFilter);

      ArrayList<Filter> result = new ArrayList<Filter>();
      result.add(this.sienaFilter);

      return result;
   }

   private void addConstraintForTripleWithLiteralObject(Triple triple) throws NetInfCheckedException {
      SienaFilterName name = buildSienaFilterName(triple);
      String value = ((Node_Literal) triple.getObject()).getLiteralLexicalForm();
      this.sienaFilter.addConstraint(name.toString(), value);
   }

   private void addConstraintForBinaryFilter(ExprFunction2 expr) throws NetInfCheckedException {
      String variable;
      NodeValue literal;

      Expr argument1 = expr.getArg1();
      Expr argument2 = expr.getArg2();

      if (argument1 instanceof ExprVar && argument2 instanceof NodeValue) {
         short op = translateOperator(expr.getOpName(), false);
         variable = ((ExprVar) argument1).getVarName();
         literal = (NodeValue) argument2;
         addConstraintForFilterWithSingleVariable(op, variable, literal);
      } else if (argument1 instanceof NodeValue && argument2 instanceof ExprVar) {
         short op = translateOperator(expr.getOpName(), true);
         variable = ((ExprVar) argument2).getVarName();
         literal = (NodeValue) argument1;
         addConstraintForFilterWithSingleVariable(op, variable, literal);
      } else if (argument1 instanceof ExprVar && argument2 instanceof ExprVar) {
         addConstraintForFilterWithTwoVariables(expr.getOpName(), (ExprVar) argument1, (ExprVar) argument2);
      } else {
         throw new SubscriptionInvalidFilterException("Binary filter with unsupported operand types: " + expr);
      }
   }

   private void addConstraintForFilterWithSingleVariable(short op, String variable, NodeValue literal)
   throws NetInfCheckedException {
      if (!this.triplesWithObjectVariable.containsKey(variable)) {
         throw new SubscriptionInvalidFilterException("Filter references the variable " + variable
               + " which does not occur in any Triple!");
      }

      Triple triple = this.triplesWithObjectVariable.get(variable);
      String sienaFilterName = buildSienaFilterName(triple).toString();

      if (literal instanceof NodeValueBoolean) {
         this.sienaFilter.addConstraint(sienaFilterName, op, ((NodeValueBoolean) literal).getBoolean());
      } else if (literal instanceof NodeValueDecimal) {
         this.sienaFilter.addConstraint(sienaFilterName, op, ((NodeValueDecimal) literal).getDouble());
      } else if (literal instanceof NodeValueDouble) {
         this.sienaFilter.addConstraint(sienaFilterName, op, ((NodeValueDouble) literal).getDouble());
      } else if (literal instanceof NodeValueFloat) {
         this.sienaFilter.addConstraint(sienaFilterName, op, ((NodeValueFloat) literal).getFloat());
      } else if (literal instanceof NodeValueInteger) {
         this.sienaFilter.addConstraint(sienaFilterName, op, ((NodeValueInteger) literal).getInteger().longValue());
      } else if (literal instanceof NodeValueString) {
         this.sienaFilter.addConstraint(sienaFilterName, op, ((NodeValueString) literal).getString());
      } else if (literal instanceof NodeValueDate) {
         this.sienaFilter.addConstraint(sienaFilterName, op, ((NodeValueDate) literal).getDate().toString());
      } else if (literal instanceof NodeValueDateTime) {
         this.sienaFilter.addConstraint(sienaFilterName, op, ((NodeValueDateTime) literal).getDate().toString());
      } else {
         throw new SubscriptionInvalidFilterException("Unsupported Filter Operand: " + literal);
      }
   }

   private void addConstraintForFilterWithTwoVariables(String opName, ExprVar argument1, ExprVar argument2)
   throws NetInfCheckedException {
      Triple triple1 = this.triplesWithObjectVariable.get(argument1.getVarName());
      SienaFilterName sienaFilterName1 = buildSienaFilterName(triple1);

      Triple triple2 = this.triplesWithObjectVariable.get(argument2.getVarName());
      SienaFilterName sienaFilterName2 = buildSienaFilterName(triple2);

      if (TranslationSiena.PREFIX_OLD.equals(sienaFilterName1.getPrefix())
            && TranslationSiena.PREFIX_OLD.equals(sienaFilterName2.getPrefix())) {
         throw new SubscriptionInvalidFilterException("The two variables ?" + argument1.getVarName() + " and ?"
               + argument2.getVarName() + " may not both reference the old InformationObject!");
      }

      if (TranslationSiena.PREFIX_NEW.equals(sienaFilterName1.getPrefix())
            && TranslationSiena.PREFIX_NEW.equals(sienaFilterName2.getPrefix())) {
         throw new SubscriptionInvalidFilterException("The two variables ?" + argument1.getVarName() + " and ?"
               + argument2.getVarName() + " may not both reference the new InformationObject!");
      }

      if (!sienaFilterName1.getComponents().equals(sienaFilterName2.getComponents())) {
         throw new SubscriptionInvalidFilterException("The two variables ?" + argument1.getVarName() + " and ?"
               + argument2.getVarName() + " may not reference distinct Attributes!");
      }

      boolean invertOperator = TranslationSiena.PREFIX_NEW.equals(sienaFilterName1.getPrefix());
      String sienaFilterNameStr = sienaFilterName1.toStringWithPrefix(TranslationSiena.PREFIX_DIFF);
      this.sienaFilter.addConstraint(sienaFilterNameStr, Op.EQ, translateOperatorToDiffStatus(opName, invertOperator));
   }

   private void addConstraintForBoundFilter(HashSet<String> addSet, HashSet<String> checkSet, E_Bound boundExpr,
         String creationIndicator) throws NetInfCheckedException {
      String variableName = boundExpr.getArg().getExprVar().getVarName();
      Triple triple = this.triplesWithObjectVariable.get(variableName);

      if (triple == null) {
         throw new SubscriptionInvalidFilterException("Filter references the variable " + variableName
               + " which does not occur in any Triple!");
      }

      SienaFilterName sienaFilterName = buildSienaFilterName(triple);

      if (checkSet.remove(sienaFilterName.toStringWithToggledPrefix())) {
         // We have already parsed this filter's dual filter, so everything is fine, add the constraint
         String value = sienaFilterName.getPrefix().equals(creationIndicator) ? TranslationSiena.STATUS_CREATED
               : TranslationSiena.STATUS_DELETED;
         this.sienaFilter.addConstraint(sienaFilterName.toStringWithPrefix(TranslationSiena.PREFIX_DIFF), Op.EQ, value);
      } else {
         // We have not yet spotted the corresponding dual filter, thus wait
         addSet.add(sienaFilterName.toString());
      }
   }

   private SienaFilterName buildSienaFilterName(Triple triple) throws NetInfCheckedException {
      SienaFilterName sienaFilterName = new SienaFilterName();
      sienaFilterName.prependComponent(getTriplePredicate(triple));
      buildSienaFilterName(sienaFilterName, triple, new HashSet<String>());
      return sienaFilterName;
   }

   private void buildSienaFilterName(SienaFilterName filterName, Triple triple, HashSet<String> usedVariables)
   throws NetInfCheckedException {
      Node uncastedSubject = triple.getSubject();

      if (!(uncastedSubject instanceof Node_Variable)) {
         throw new SubscriptionInvalidTripleSubjectException("Triple Subject has to be a Variable: " + triple);
      }

      String variableName = ((Node_Variable) uncastedSubject).getName();

      if (TranslationSiena.PREFIX_OLD.equals(variableName) || TranslationSiena.PREFIX_NEW.equals(variableName)) {
         filterName.setPrefix(variableName);
      } else if (TranslationSiena.PREFIX_OLD_WILDCARD.equals(variableName)) {
         filterName.setPrefix(TranslationSiena.PREFIX_OLD);
         filterName.prependComponent(TranslationSiena.WILDCARD);
      } else if (TranslationSiena.PREFIX_NEW_WILDCARD.equals(variableName)) {
         filterName.setPrefix(TranslationSiena.PREFIX_NEW);
         filterName.prependComponent(TranslationSiena.WILDCARD);
      } else if (this.triplesWithObjectVariable.containsKey(variableName)) {
         if (usedVariables.contains(variableName)) {
            throw new SubscriptionInfiniteVariableRecursionException("Infinite Recursion: The variable ?" + variableName
                  + " references itself!");
         }

         usedVariables.add(variableName);

         Triple referredTriple = this.triplesWithObjectVariable.get(variableName);
         filterName.prependComponent(getTriplePredicate(referredTriple));
         buildSienaFilterName(filterName, referredTriple, usedVariables);
      } else {
         throw new SubscriptionInvalidTripleSubjectException("Unknown Triple Subject variable: ?" + variableName);
      }
   }

   private String getTriplePredicate(Triple triple) throws NetInfCheckedException {
      Node uncastedPredicate = triple.getPredicate();

      if (!(uncastedPredicate instanceof Node_URI)) {
         throw new SubscriptionInvalidTriplePredicateException("Triple Predicate has to be a URI: " + triple);
      }

      return ((Node_URI) uncastedPredicate).getURI();
   }

   private short translateOperator(String sparqlOperator, boolean invertOperator) {
      if (sparqlOperator.equals(SPARQL_EQ)) {
         return Op.EQ;
      } else if (sparqlOperator.equals(SPARQL_NE)) {
         return Op.NE;
      } else if (sparqlOperator.equals(SPARQL_GT)) {
         return invertOperator ? Op.LT : Op.GT;
      } else if (sparqlOperator.equals(SPARQL_LT)) {
         return invertOperator ? Op.GT : Op.LT;
      } else if (sparqlOperator.equals(SPARQL_GE)) {
         return invertOperator ? Op.LE : Op.GE;
      } else if (sparqlOperator.equals(SPARQL_LE)) {
         return invertOperator ? Op.GE : Op.LE;
      }
      throw new NetInfUncheckedException("Unknown SparQL operator: " + sparqlOperator);
   }

   private String translateOperatorToDiffStatus(String sparqlOperator, boolean invertOperator)
   throws SubscriptionInvalidFilterOperatorException {
      if (sparqlOperator.equals(SPARQL_EQ)) {
         return TranslationSiena.STATUS_UNCHANGED;
      } else if (sparqlOperator.equals(SPARQL_NE)) {
         return TranslationSiena.STATUS_CHANGED;
      } else if (sparqlOperator.equals(SPARQL_GT)) {
         return invertOperator ? TranslationSiena.STATUS_CHANGED_LT : TranslationSiena.STATUS_CHANGED_GT;
      } else if (sparqlOperator.equals(SPARQL_LT)) {
         return invertOperator ? TranslationSiena.STATUS_CHANGED_GT : TranslationSiena.STATUS_CHANGED_LT;
      } else if (sparqlOperator.equals(SPARQL_GE)) {
         throw new SubscriptionInvalidFilterOperatorException("The operator >= is not supported!");
      } else if (sparqlOperator.equals(SPARQL_LE)) {
         throw new SubscriptionInvalidFilterOperatorException("The operator <= is not supported!");
      }
      throw new NetInfUncheckedException("Unknown SparQL operator: " + sparqlOperator);
   }
}
