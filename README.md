# external-ip-publisher
##About
 The external IP Address publisher is a java based application runs on the Windows only. The publisher is registered 
as a windows service and polling your computer external IP address and in case then IP address change, the service 
sends the e-mail notification regarding your new IP Address.

##Configuration
 The configuration defined in the ip-notifier.ini file. Please follow instruction according the my comment.
##Password Encryption
  One of the configuration properties its your email password, which you need to create by the following command
  java com.ip.notifier.StringCryptor <command> <text>
  command :
		-e - encrypt text.
		-d - decrypt text.

##Execute
1. Copy deploy folder to your computer. 
2. To create IP Notifier service run : ip-notifier.exe --WinRun4J:RegisterService.
3. Open "Services" application and start the IP Notifier service.
4. Delete IP Notifier service run : ip-notifier.exe --WinRun4J:UnregisterService.

###The application uses the winrun4j tool http://winrun4j.sourceforge.net for the Windows service functionality handling.




  
 
