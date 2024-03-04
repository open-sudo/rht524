package org.acme;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Enumeration;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.attribute.PosixFilePermission;
import java.nio.file.attribute.*;
import java.io.IOException;
import java.util.Set;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Parameters;
import java.io.*;
import java.util.*;
import java.util.stream.Collectors;
import java.nio.*;
import java.nio.file.*;
import org.apache.commons.io.FileUtils;
import java.util.regex.*;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileOwnerAttributeView;
import java.io.IOException;
import java.io.BufferedReader;
import java.io.InputStreamReader;
@Command(name = "greeting", mixinStandardHelpOptions = true)
public class GreetingCommand implements Runnable {
   String groups[]=new String []{"developers","readers","testers","admins"};
   String serviceAccount="appservice";
   String users[]=new String []{"alicej","bobsmith","carold","davew","emilys"};
   String names[]=new String []{"Alice Johnson","Bob Smith","Carol Danvers","Dave Wilson","Emily Stark"};

   String dirs [] = new String []{"/project/admins","/project/developers","/project/testers","/project/shared","/project/shared/scripts","/project/admins/shared.txt","/project/testers/shared.txt","/project/developers/shared.txt"};

  String [] links = new String []{"/project/admins/scripts","/project/testers/scripts","/project/developers/scripts"};
  String shadow="/etc/shadow";
  String key="EFoG9L4sh1LeSYAxo/wGL7Arek0lydq3";

public String getMacAddress()throws Exception{

       // Get the local host
            InetAddress address = InetAddress.getLocalHost();
            
            // Get the network interface for the local host
            NetworkInterface ni = NetworkInterface.getByInetAddress(address);
            
            // If a network interface is identified
            if (ni != null) {
                byte[] mac = ni.getHardwareAddress();
                if (mac != null) {
                    // Convert byte array to MAC address string
                    StringBuilder sb = new StringBuilder();
                    for (int i = 0; i < mac.length; i++) {
                        sb.append(String.format("%02X%s", mac[i], (i < mac.length - 1) ? "-" : ""));
                    }
		    return sb.toString();
                } else {
//                    System.out.println("Address doesn't exist or is not accessible.");
                }
            } else {
  //              System.out.println("Network Interface for the specified address is not found.");

            }
	return null;
}

public List<String> getGroups(String username) throws Exception{
            Process process = Runtime.getRuntime().exec(new String[]{"groups", username});
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));            
            String line = reader.readLine();
            if (line != null) {
                String[] parts = line.split(":");
                if (parts.length > 1) {
                    String[] groups = parts[1].trim().split("\\s+");
                    System.out.println("Groups for user " + username + ":"+Arrays.asList(groups));
		    return Arrays.asList(groups);
                }
            }
            reader.close();
            BufferedReader stdError = new BufferedReader(new InputStreamReader(process.getErrorStream()));
            String err;
            while ((err = stdError.readLine()) != null) {
                System.out.println("Error: " + err);
            }
            stdError.close();
            process.waitFor();
	    return null;
}

public float  userGroupMembership() throws Exception{
   float grade=0;
   float inc=1f;
   List<String> gps=getGroups("carold");
   if(gps!=null && gps.contains("developers")){
	grade+=inc;
   }
   gps=getGroups("bobsmith");
   if(gps!=null && gps.contains("admins")){
	grade+=inc;
   }
   gps=getGroups("alicej");
   if(gps!=null && gps.contains("admins")){
	grade+=inc;
   }
   gps=getGroups("davew");
   if(gps!=null && gps.contains("testers")){
	grade+=inc;
   }
   gps=getGroups("emilys");
   if(gps!=null && gps.contains("testers")){
	grade+=inc;
   }

   return grade;
}
public float validateOwnerships() throws Exception{
	    float grade=1;
            float  inc=1;
	    String folderNames []= new String []{"admins","testers","developers","shared"};	
	   for(String folderName:folderNames){
	        String folder="/project/"+folderName;
		if(!new File(folder).exists()){
			System.out.println("No folder "+folder);
			continue;
		}
		PosixFileAttributes attrs = Files.readAttributes(Paths.get(folder), PosixFileAttributes.class);
            	String groupOwner = attrs.group().getName();
            	if(groupOwner.equals(folderName) || (groupOwner.equals("admins") && folderName.equals("shared"))){
			System.out.println("Correct group owner found for folder "+folder);
                	grade+=inc;
	    	}else{
			System.out.println("Wrong group owner found for folder "+folder+" -> "+groupOwner);
	   	}
	   }
	   return grade;
}

String expected="rwxrwx---";

public float validateSetUidPermission() throws Exception{
	float grade=0f;
        float inc=2f;
		 String folder="/project/developers";
		 if(!new File(folder).exists()){
                        System.out.println("No folder "+folder);
                        return 0;
                  }

	         PosixFileAttributes attrs = Files.readAttributes(Paths.get(folder), PosixFileAttributes.class);
            	 Set<PosixFilePermission> permissions = attrs.permissions();
            	 String permission= PosixFilePermissions.toString(permissions);
            	 if(permission.equals(expected)){
			System.out.println("Correct permission found for folder "+folder);
                	grade+=inc;
	    	 }else{
			System.out.println("Wrong permission found for folder "+folder+" -> "+permission+". Expected:"+expected);
	   	 }
	return grade;
}
public float validatePermissions() throws Exception{
	    float grade=0;
            float  inc=2;
	    String folderNames []= new String []{"admins","testers","shared","developers"};	
	   for(String folderName:folderNames){
		 String folder="/project/"+folderName;
              if(!new File(folder).exists()){
                        System.out.println("No folder "+folder);
                        continue;
                }

	         PosixFileAttributes attrs = Files.readAttributes(Paths.get(folder), PosixFileAttributes.class);
            	 Set<PosixFilePermission> permissions = attrs.permissions();
            	 String permission= PosixFilePermissions.toString(permissions);
            	 if(permission.equals(expected)){
			System.out.println("Correct permission found for folder "+folder);
                	grade+=inc;
	    	 }else{
			System.out.println("Wrong permission found for folder "+folder+" -> "+permission);
	   	 }
	   }

	   return grade;
}

public float getPasswordChangeFrequency() throws Exception{
          float grade=3.25f;          
	  InputStream inputStream = new FileInputStream(shadow);
           BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
           List<String> contents = reader.lines().collect(Collectors.toList());
   	   String shadowEntry=null;
	 for(String content:contents){
   	 	if(content.startsWith("carold:")){
			shadowEntry=content;
		}
    	  }
         if(shadowEntry==null){
		System.out.println("No password found for user carold :(");
	 	return 0; 
	 }
    String[] fields = shadowEntry.split(":");
    if (fields.length > 4 && !fields[4].isEmpty()) {        
	float freq= Integer.parseInt(fields[4]);
        if(freq==30){
            System.out.println("Correct password change frequency found for user carold:"+freq);
	    return grade;
	}else{
	    System.out.println("Wrong password change ferquency found:"+freq);
	}
    }
    return 0;
}



public float getDaysUntilExpiryFromShadow() throws Exception{
          float grade=3;          
	  InputStream inputStream = new FileInputStream(shadow);
           BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
           List<String> contents = reader.lines().collect(Collectors.toList());
   	   String shadowEntry=null;
	 for(String content:contents){
   	 	if(content.startsWith("emilys:")){
			shadowEntry=content;
		}
    	  }
         if(shadowEntry==null){
		System.out.println("No password found for user emilys :(");
	 	return 0; 
	 }
    String[] fields = shadowEntry.split(":");
        if (fields.length > 7 && !fields[7].isEmpty()) {
            long expiryDaysSinceEpoch = Long.parseLong(fields[7]);
            if (expiryDaysSinceEpoch > 0) {
                LocalDate epoch = LocalDate.of(1970, 1, 1);
                LocalDate expiryDate = epoch.plusDays(expiryDaysSinceEpoch);
                LocalDate now = LocalDate.now();
                long days= ChronoUnit.DAYS.between(now, expiryDate);
                if(days>150l){
            		System.out.println("Correct password expiry found for user emilys:"+days);
	  		  return grade;
		}else{
	    		System.out.println("Wrong password expiry found for user emilys:"+days);
		}

            }
        }else{
	    		System.out.println("No password expiry found for user emilys");

	}

    return 0;
}














  public float checkPasswordChangeRequirement() throws Exception{
          InputStream inputStream = new FileInputStream(shadow);
           BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
           List<String> contents = reader.lines().collect(Collectors.toList());
	   float grade=0;
           float inc=1.75f;
	   for(String user:users){
		for(String content:contents){
		   if(content.startsWith(user+":")){
                     float lastPassChangeDate=getLastPasswordChange(content);
                     if(lastPassChangeDate==0){
                     	    grade=grade+inc;
			     System.out.println("User "+user+" is correctly configured to change password");
		     }

		   }
		}
	   }

	return grade;
  }
   public float getLastPasswordChange(String shadowEntry) {
        try {
            String[] fields = shadowEntry.split(":");
            if (fields.length > 2 && !fields[2].isEmpty()) {
                return Integer.parseInt(fields[2]);
            }
        } catch (NumberFormatException e) {
            System.err.println("Error parsing the last password change date.");
        }
        return -1;
    }

  public String getContent(String folder) {
        File dir = new File(folder);
        String content="";
	Collection<File> files = FileUtils.listFiles(dir, null, true); // No filter for file extensions
        for (File file : files) {
            try {
		String fileContent=FileUtils.readFileToString(file, "UTF-8");
                content += fileContent;
	} catch (IOException e) {
                e.printStackTrace();
            }
        }
	return content;
    }

public boolean matchesUmask(String content){
	Pattern pattern = Pattern.compile(".*umask\\s*00?27.*", Pattern.DOTALL);
        Matcher matcher = pattern.matcher(content);
	   return  matcher.matches();
}


 public boolean  pathContainsSharedScripts(){
    String paths[]=System.getenv("PATH").split(":");
    for(String path:paths){
        if(path.endsWith("/")){
		path=path.substring(0,path.length()-1);
	}
        if(path.equals("/project/shared/scripts")){
		return true;
	}
    }
    return false;
 }
  public float folderCreated(String []dirs){
        float grade=0;
        float inc=2.5f;
        for(String dir:dirs){
          File dirObject= new File(dir);
         if(!dirObject.exists()){
           System.out.println("File not found:"+dir);
	 }else{
           System.out.println("File found:"+dir);
	   grade+=inc;
	}
        }
	return grade;
  }

 public float checkSoftLinks(String links[]){
	float grade=0.5f;
        float inc=1.5f;
        for(String link:links){
		if(Files.isSymbolicLink(Path.of(link))){
        	   System.out.println("Soft link found:"+link);
	   	   grade+=inc;
		}else{
           		System.out.println("No soft link found:"+link);
		}
	}
	return grade;
 }

  public boolean adminGroupSudo() throws Exception{
        String content= getContent("/etc/sudoers.d").toLowerCase();
	Pattern pattern = Pattern.compile(".*%admins\\s*all.*", Pattern.DOTALL);
        Matcher matcher = pattern.matcher(content);
	if(matcher.matches()){
		return true;
	}
        return false;
  }
   public boolean  userCreated(String username, List<String> content){
	for(String user: content){
              if(user.contains(username)){
                 return true;
              }
   	}
       return false;
   }
   public boolean  hasServiceAccount(List<String> content){
	for(String user: content){
              if(user.contains(serviceAccount) && user.contains("/usr/sbin/nologin")){
                 return true;
              }
   	}
       return false;
   }

   public float  groupCreated(List<String> content){
	float grade=0;
        float inc=1;   
	for(String groupname:groups){
                boolean groupFound=false;
		for(String group: content){
        	      if(group.contains(groupname)){
                	  groupFound=true;
			  grade+=inc;
              		}
   		 }
                 if(!groupFound){
                         System.out.println("No group found:"+groupname);
                  }else{
                         System.out.println("Group found:"+groupname);
                  }
         }
       return grade;
   }

  public  float userNameSet(List<String> contents){
        float grade=0;
        float inc=1;
	for(int count=0; count<names.length; count++){
		   boolean userFound=userNameSet(users[count],names[count],contents);
		   if(!userFound && names[count].trim().length()>0){
  			System.out.println("No user name found:"+names[count]);
        	   }else{
	  		System.out.println("User name found:"+names[count]);
			grade+=inc;
		   }
	}
	return grade;
  }  
  public  boolean userNameSet(String username, String name, List<String> content){
	for(String user: content){
              if(user.contains(username)){
                String [] parts =name.split(" ");
                for(String part:parts){
                    if(!user.toLowerCase().contains(part.toLowerCase())){
			return false;
		    }
                }
                 return true;
              }
   	}
       return false;
  }

  public float userCreated(List<String> contents){
     float grade=0;
     float inc=1;
	  for(String user:users){
		   boolean userFound=userCreated(user,contents);
		   if(!userFound){
  			System.out.println("No user found with username:"+user);
        	   }else{
	  		System.out.println("User found with username:"+user);
                        grade+=inc;
		   }
	}
	return grade;
}


public String encrypt(String  content){

 try {
            // The Base64 encoded representation of the secret key
            
            // Decode the Base64 encoded key
            byte[] decodedKey = Base64.getDecoder().decode(key);
            
            // Rebuild key using SecretKeySpec
            SecretKeySpec secretKey = new SecretKeySpec(decodedKey, 0, decodedKey.length, "AES");
            
            // Text to encrypt
            String originalText = content;
            
            // Cipher Initialization for Encryption
            Cipher cipher = Cipher.getInstance("AES");
            cipher.init(Cipher.ENCRYPT_MODE, secretKey);
            
            // Perform Encryption
            byte[] encryptedBytes = cipher.doFinal(originalText.getBytes());
            
            // Convert encrypted bytes to Base64 to get a string result
            String encryptedTextBase64 = Base64.getEncoder().encodeToString(encryptedBytes);
            
            return  encryptedTextBase64;
            
        } catch (Exception e) {
            System.err.println("Error during AES encryption: " + e.toString());
        }
  return null;
}
public  void generateSignedContent(String file, String mac) throws Exception{
      String plain =FileUtils.readFileToString(new File(file),"UTF-8");
      String encrypted=plain+"\n===\n"+encrypt(plain+"\n"+mac);
      FileOutputStream signed= new FileOutputStream("encrypted.txt");
      signed.write(encrypted.getBytes());
      signed.close();
}
public float umaskConfiguredGrade() throws Exception{
    float grade=10;
             
     String content =FileUtils.readFileToString(new File("/etc/profile"),"UTF-8");
        if(matchesUmask(content)){
          grade-=4;
  	  System.out.println("Umask set");
          return grade;
        }

	content=getContent("/etc/profile.d"); 
        if(matchesUmask(content)){
  	  System.out.println("Umask set appropriately");
          return grade;
        }
	content = FileUtils.readFileToString(new File("/etc/bashrc"),"UTF-8");
        if(matchesUmask(content)){
          grade-=4;
  	  System.out.println("Umask set");
           return grade;
        }
  	System.out.println("Umask not set appropriately");
	return 0;
}

public float serviceAccountGrade(List<String> contents){
	float grade=0;
        float inc=3;
		   boolean  svc=hasServiceAccount(contents);
		   if(!svc){
  			System.out.println("No service account found:"+serviceAccount);
        	   }else{
	  		System.out.println("Service account found:"+serviceAccount);
		  	grade+=inc;
		   }
	return grade;
}  

public float pathSettingGrade(){
 float grade=0;
 float inc=5;
		   String projectPath=System.getenv("PROJECT_PATH");

          	   if(projectPath==null || projectPath.equals("") || !projectPath.equals("/project")){
	  		System.out.println("No PROJECT_PATH env variable found");
		   }else{
	  		System.out.println("PROJECT_PATH env variable found:"+projectPath);
                        grade+=inc;
		  }

                  boolean checkPath=pathContainsSharedScripts();
		   if(!checkPath){
  			System.out.println("No shared script folder found in path");
        	   }else{
  			System.out.println("Shared script folder found in path");
			grade+=inc;
		   }
	return grade;
}

public float sudoGrade() throws Exception{
  float grade=0;
  float inc=3;
	           boolean admin=adminGroupSudo();
		   if(!admin){
  			System.out.println("No sudo permissions found for group admins");
        	   }else{
	  		System.out.println("Sudo permissions found for group admins");
			grade+=inc;
		   }
 return grade;

}
    @Override
    public void run(){
     try{

	 PrintStream fileOut = new PrintStream(new FileOutputStream("output.txt"));
         System.setOut(fileOut);

	  InputStream inputStream = new FileInputStream("/etc/passwd");
	  BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
          List<String> contents = reader.lines().collect(Collectors.toList());

	  InputStream inputStreamGrp = new FileInputStream("/etc/group");
	  BufferedReader readerGrp = new BufferedReader(new InputStreamReader(inputStreamGrp));
          List<String> contentGrp = readerGrp.lines().collect(Collectors.toList());
		   float sudo=sudoGrade();
                   float path=pathSettingGrade();
                   float umask=umaskConfiguredGrade();
                   float svc=serviceAccountGrade(contents);
                   float userCreation=userCreated(contents);
		   float groupCreation=groupCreated(contentGrp);	

		   float fileCreation=folderCreated(dirs);
		   float linkCreation=checkSoftLinks(links);

		   float login=checkPasswordChangeRequirement();
		   float freq=getPasswordChangeFrequency();

		   float days=getDaysUntilExpiryFromShadow();
  		   float ownership=validateOwnerships();
		   float permis=validatePermissions();
        	   float userNames=userNameSet(contents); 
                   float membership=userGroupMembership();
                   float guid=validateSetUidPermission();

	          System.out.println("\n\n");
	          System.out.println("Group memberships:"+membership);
                  System.out.println("Group creation:"+groupCreation);
	          System.out.println("Users full names:"+userNames);
	          System.out.println("User creation:"+userCreation);
	          System.out.println("Service account:"+svc);
	          System.out.println("Sudo config:"+sudo+"\n");


                   System.out.println("Links creation:"+linkCreation);
                   System.out.println("File creation:"+fileCreation+"\n");

                   System.out.println("SGID Permission:"+guid);
                   System.out.println("File permission:"+permis);
	          System.out.println("File ownership:"+ownership+"\n");


	          System.out.println("Env variables:"+path);
	          System.out.println("Umask:"+umask);
                   System.out.println("Password expiry:"+days);
                   System.out.println("Password change frequency:"+freq);
                   System.out.println("Password change on login:"+login);
		   generateSignedContent("./output.txt",getMacAddress());
      }catch(Exception e){
      e.printStackTrace();
    }
    }

}
