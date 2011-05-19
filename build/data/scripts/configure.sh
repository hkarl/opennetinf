#!/bin/bash
source ~/.netinf/settings
C=${DP_LOC}/configs

echo "This script configures the compiled distribution in ${DP_LOC}"
echo "from values in ~/.netinf/settings"

echo ""
echo "Configuring Scenario 1, Node A"
CURFILE="${C}/scenario1/netinfnode_a.properties"
echo "Modifying ${CURFILE}"
sed -i "s|access.tcp.port = .*|access.tcp.port = ${SC1_PO_NODEA}|g" "${CURFILE}"
sed -i "s|access.http.port = .*|access.http.port = ${SC1_PH_NODEA}|g" "${CURFILE}"
sed -i "s|cc.tcp.port = .*|cc.tcp.port = ${SC1_PO_NODEA}|g" "${CURFILE}"
sed -i "s|resolution.cache.httpIP = .*|resolution.cache.httpIP = ${SC1_IP_NODEA}|g" "${CURFILE}"
sed -i "s|communicator_port=.*|communicator_port=${SC1_PO_NODEA}|g" "${CURFILE}"
sed -i "s|search_rdf_db_user = .*|search_rdf_db_user = ${DB_SDBUSER}|g" "${CURFILE}"
sed -i "s|search_rdf_db_pw = .*|search_rdf_db_pw = ${DB_SDBPASS}|g" "${CURFILE}"
sed -i "s|resolution_rdf_db_user = .*|resolution_rdf_db_user = ${DB_SDBUSER}|g" "${CURFILE}"
sed -i "s|resolution_rdf_db_pw = .*|resolution_rdf_db_pw = ${DB_SDBPASS}|g" "${CURFILE}"
sed -i "s|netinf.gp.interface.port = 6666|netinf.gp.interface.port = ${SC1_PO_GPSOCK}|g" "${CURFILE}"

echo ""
echo "Configuring Scenario 1, Node B"
CURFILE="${C}/scenario1/netinfnode_b.properties"
echo "Modifying ${CURFILE}"
sed -i "s|access.tcp.port = .*|access.tcp.port = ${SC1_PO_NODEB}|g" "${CURFILE}"
sed -i "s|access.http.port = .*|access.http.port = ${SC1_PH_NODEB}|g" "${CURFILE}"
sed -i "s|cc.tcp.port = .*|cc.tcp.port = ${SC1_PO_NODEB}|g" "${CURFILE}"
sed -i "s|resolution.cache.httpIP = .*|resolution.cache.httpIP = ${SC1_IP_NODEB}|g" "${CURFILE}"
sed -i "s|communicator_port=.*|communicator_port=${SC1_PO_NODEB}|g" "${CURFILE}"
sed -i "s|search_rdf_db_user = .*|search_rdf_db_user = ${DB_SDBUSER}|g" "${CURFILE}"
sed -i "s|search_rdf_db_pw = .*|search_rdf_db_pw = ${DB_SDBPASS}|g" "${CURFILE}"
sed -i "s|resolution_rdf_db_user = .*|resolution_rdf_db_user = ${DB_SDBUSER}|g" "${CURFILE}"
sed -i "s|resolution_rdf_db_pw = .*|resolution_rdf_db_pw = ${DB_SDBPASS}|g" "${CURFILE}"
sed -i "s|netinf.gp.interface.port = 6666|netinf.gp.interface.port = ${SC1_PO_GPSOCK}|g" "${CURFILE}"

echo ""
echo "Configuring Scenario 1, Node C"
CURFILE="${C}/scenario1/netinfnode_c.properties"
echo "Modifying ${CURFILE}"
sed -i "s|access.tcp.port = .*|access.tcp.port = ${SC1_PO_NODEB}|g" "${CURFILE}"
sed -i "s|access.http.port = .*|access.http.port = ${SC1_PH_NODEB}|g" "${CURFILE}"
sed -i "s|cc.tcp.port = .*|cc.tcp.port = ${SC1_PO_NODEB}|g" "${CURFILE}"
sed -i "s|communicator_port=.*|communicator_port=${SC1_PO_NODEB}|g" "${CURFILE}"
sed -i "s|netinf.gp.interface.port = 6666|netinf.gp.interface.port = ${SC1_PO_GPSOCK}|g" "${CURFILE}"

echo ""
echo "Configuring Scenario 1, Logging"
for CURFILE in "${C}/scenario1/log4j/demoNodeA.xml" "${C}/scenario1/log4j/demoNodeB.xml" "${C}/scenario1/log4j/demoNodeC.xml"; do
	echo "Modifying ${CURFILE}"
	sed -i "s|RemoteHost\" value=\"[0-9\.]*\"|RemoteHost\" value=\"${LOGSERVER}\"|g" "${CURFILE}"
	sed -i "s|Port\" value=\"[0-9]*\"|Port\* value=\"${LOGPORT}\"|g" "${CURFILE}"
done

echo ""
echo "Configuring Scenario 1, Remote RS"
for CURFILE in "${C}/scenario1/remoteRS/1.remoteRS" "${C}/scenario1/remoteRS_a/1.remoteRS" "${C}/scenario1/remoteRS_b/1.remoteRS"; do
	echo "Modifying ${CURFILE}"
	sed -i "s|host=.*|host=${SC1_IP_NODEC}|g" "${CURFILE}"
	sed -i "s|port=.*|port=${SC1_PO_NODEC}|g" "${CURFILE}"
done

echo ""
echo "Configuring Scenario 2, ESF Users"
CURFILE="${C}/scenario2/eventServiceSiena.properties"
sed -i "s|database.user = .*|database.user = ${DB_EVSUSER}|g" "${CURFILE}"
sed -i "s|database.password = .*|database.password = ${DB_EVSPASS}|g" "${CURFILE}"

echo ""
echo "Configuring Scenario 2, SDB Users"
CURFILE="${C}/scenario2/searchRdfNode.properties"
sed -i "s|search_rdf_db_user = .*|search_rdf_db_user = ${DB_SDBUSER}|g" "${CURFILE}"
sed -i "s|search_rdf_db_pw = .*|search_rdf_db_pw = ${DB_SDBPASS}|g" "${CURFILE}"
sed -i "s|resolution_rdf_db_user = .*|resolution_rdf_db_user = ${DB_SDBUSER}|g" "${CURFILE}"
sed -i "s|resolution_rdf_db_pw = .*|resolution_rdf_db_pw = ${DB_SDBPASS}|g" "${CURFILE}"

echo ""
echo "Configuring Scenario 2, Logging"
for CURFILE in "${C}/scenario2/log4j/eventServiceSiena.xml" "${C}/scenario2/log4j/globalRSNode.xml" "${C}/scenario2/log4j/managementTool.xml" "${C}/scenario2/log4j/productlistCheckout.xml" "${C}/scenario2/log4j/productlistGirlfriendPeter.xml" "${C}/scenario2/log4j/productlist.xml" "${C}/scenario2/log4j/searchRdfNode.xml" "${C}/scenario2/log4j/shoppingJack.xml" "${C}/scenario2/log4j/shoppingPeter.xml" "${C}/scenario2/log4j/shopping.xml"; do
	echo "Modifying ${CURFILE}"
	sed -i "s|RemoteHost\" value=\"[a-zA-Z0-9\.]*\"|RemoteHost\" value=\"${LOGSERVER}\"|g" "${CURFILE}"
	sed -i "s|Port\" value=\"[0-9]*\"|Port\* value=\"${LOGPORT}\"|g" "${CURFILE}"
done
