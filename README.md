# camel-debug-queue
if needs to exclude some message from being processed on server side just add a selector in the environment variable like below and restart the service

**JMS_DEBUG_SELECTOR** : `?selector=MCP_SYSTEM_HEADER NOT LIKE '%tradeRefNbr":"JM001%'`
