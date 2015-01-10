# external-ip-publisher
##About
External IP Address publisher is a java based application runs on the Windows only. The publisher is register
as a windows service, polls your computer external IP address and in case it's change, the service 
sends e-mail notification with your new IP Address to the configured destination.

##Configuration
Configuration defined in the ip-notifier.ini file. Please follow my comments.

###Password Encryption
One of the configuration properties its your email password, which shall be encrypted by next command: 
java com.ip.notifier.StringCryptor <option> <text> 
Possible options :
-e - encrypt text.
-d - decrypt text.

##Execute
1. Copy deploy folder to your computer.
2. Configure ip-notifier.ini file
2. Execute from cli : ip-notifier.exe --WinRun4J:RegisterService.
3. Open "Services" application and start the IP Notifier service.
P.S 
 In order to delete IP Notifier service you shall run - ip-notifier.exe --WinRun4J:UnregisterService.

#####The application uses the winrun4j tool http://winrun4j.sourceforge.net for the Windows service functionality handling.




  
 
