# akka-http-google-login
Simple template to enable an akka-http project with Google login.

In order to run the embedded server you need to set the environment variable GOOGLE_CLIENT_ID to your client ID 
as provided to you by Google; and GOOGLE_SECRET to the respective client secret. The server listens on localhost
port 8080 by default.

Please treat this code as experimental; it contains no tests, it is work-in-progress, and may have security problems.
DO NOT use it on your production software, unless you have audited it and know what you're doing.
