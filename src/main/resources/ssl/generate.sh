# cleanup if necessary
rm -f *.p12 *.cer *.jks

# Generate the tomcat key
keytool -genkey -dname "CN=localhost,OU=Pivotal,O=Spring,L=Chicago,ST=IL,C=US" -keypass password -storepass password -alias tomcat -keyalg RSA -keystore server.jks

# Generate the client certificate
keytool -genkey -dname "CN=rob,OU=Pivotal,O=Spring,L=Chicago,ST=IL,C=US" -alias rob -keyalg RSA -keypass password -storepass password -keystore client_keystore.p12 -storetype PKCS12

# export the client certificate
keytool -export -alias rob -storepass password -file rob.cer -keystore client_keystore.p12

# import the client certificate into the server-truststore
keytool -import -noprompt -v -trustcacerts -alias rob -file rob.cer -keystore server-truststore.jks -keypass password -storepass password -storetype PKCS12

keytool -v -importkeystore -srckeystore server.jks -srcstoretype JKS -destkeystore client_keystore.p12 -deststoretype PKCS12 -deststorepass password -srcstorepass password -srcalias tomcat -destalias localhost