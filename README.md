# camel-debug-queue
if needs to run debug on server side just add a selector in the environment
JMS_DEBUG_SELECTOR = ?selector=MCP_SYSTEM_HEADER NOT LIKE '%tradeRefNbr":"JM001%'
