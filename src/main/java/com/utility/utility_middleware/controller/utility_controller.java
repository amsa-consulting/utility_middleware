package com.utility.utility_middleware.controller;

import com.utility.utility_middleware.model.*;
import com.utility.utility_middleware.service.MyUserDetailsService;
import com.utility.utility_middleware.util.jwtutil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.io.*;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.json.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
public class utility_controller {
    @Value("${request.url}")
    private String REQUEST_URL;
    @Value("${request_clean.url}")
    private String REQUESTL_CLEAN_URL;

    //@Value("${access.url}")
   // private String BASE_URL;
    private final RestTemplate restTemplate;
    Logger logger = LoggerFactory.getLogger(utility_controller.class);
    @Autowired
    private AuthenticationManager authenticationManager;
    @Autowired
    private MyUserDetailsService userDetailsService;
    @Autowired
    private jwtutil jwtTokenUtil;

    //from properties file
    @Value("${access.email}")
    private String USER_NAME1;
    //from properties file
    @Value("${access.password}")
    private String PASSWORD1;

    public utility_controller(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @PostMapping("/api/login")
    public ResponseEntity<?> createAuthenticationToken(@RequestBody authenticationRequest authRequest) throws Exception {
        logger.info("/api/login Endpoint accessed");
      /*
       try {
           authenticationManager.authenticate(
                   new UsernamePasswordAuthenticationToken(authRequest.getUsername(), authRequest.getPassword())
           );
       }catch (BadCredentialsException e){
           throw new Exception("Incorrect username and password", e);
       }
      */
        if(authRequest.getEmail().equals(USER_NAME1) && authRequest.getPassword().equals(PASSWORD1)) {

            final UserDetails userDetails = userDetailsService       //we give the username the user put in the request body and
                    .loadUserByUsername(authRequest.getEmail());      //send it to the loadUserByUsername method of MyUserServicesClass
            //to get the User object with Username, password and Authorities
            final String access_token = jwtTokenUtil.generateToken(userDetails);  //pass the userDetail object to the jwtutil class's generateToken
            //method which calls the creatToken method of the same class to generate
            //the token
            //  final String refresh_token = jwtTokenUtil.generateRefreshToken(userDetails);
            return ResponseEntity.ok(new authenticationResponse(access_token)); //ResponseEntity represents an HTTP response, including headers, body, and status. While @ResponseBody puts the return value into the body of the response, ResponseEntity also allows us to add headers and status code.
        }else{
            AuthError authError= new AuthError();
            //return authError.setAuthenticationError("Wrong User and Password Combination");
            return ResponseEntity.ok(new AuthError());
        }

    }
 //Endpoint to clean the data using melissa endpoint https://api.amsaconnect.com:2006/melissa/clean
 // and write the payload in to creator endpoint https://creator.zoho.com/api/v2/msimpkins/aimscc-connect/form/AIMS_EDQ_Connect_Successful_Records
    @PostMapping(value = "/aims/v1/oauth/amsa",
            produces = { MediaType.APPLICATION_JSON_VALUE}
    )
    public //@ResponseBody
    ResponseMessage aimsResponse(@RequestBody Utility_middleware utility_middleware)  throws Exception {
       // logger.info("/{client}/{version}/{auth}/{collection} Endpoint accessed");

       // Utility_middleware utility_middleware1 = new Utility_middleware();

        String workdayID = utility_middleware.getWorkdayID();
        String addressType = utility_middleware.getAddressType();
        String addressLine1= utility_middleware.getAddressLine1();
        String addressLine2=utility_middleware.getAddressLine2();
        String city = utility_middleware.getCity();
        String state = utility_middleware.getState();
        String zip = utility_middleware.getZip();
        String country = utility_middleware.getCountry();


        String melissaGetUrl="https://api.amsaconnect.com:2006/melissa/clean?id=b-Zkh52ShfwlydnAyC7aCS**nSAcwXpxhQ0PC2lXxuDAZ-**&id1=120456320&opt=false&a="+addressLine1+"&city="+city+"&state="+state+"&zip="+zip+"&country="+country+"&t="+workdayID+"*"+addressType;

        String melissa_response =restTemplate.exchange(melissaGetUrl, HttpMethod.GET, null, String.class).getBody();

        JSONObject melissa_response_json = new JSONObject(melissa_response);
        String code="";
        String message="";

        String company1 = melissa_response_json.get("Company").toString();
        String addressLine11 = melissa_response_json.get("AddressLine1").toString();
        String addressLine21 = melissa_response_json.get("AddressLine2").toString();
        String city1 = melissa_response_json.get("City").toString();
        String state1 = melissa_response_json.get("State").toString();
        String postalCodeFull = melissa_response_json.get("PostalCodeFull").toString();
        String countyName = melissa_response_json.get("CountyName").toString();
        String errorString = melissa_response_json.get("ErrorString").toString();
        String suggestionList = melissa_response_json.get("SuggestionList").toString().replace("[","").replace("]","");

        String creatorPostUrl="https://creator.zoho.com/api/v2/msimpkins/aimscc-connect/form/AIMS_EDQ_Connect_Successful_Records";
        String creator_token_url="https://accounts.zoho.com/oauth/v2/token?refresh_token=1000.bf9ab0e55d1be092dbf876b5f20deaeb.d12303b316f7f941e7bb06d6bf56204a&client_id=1000.ZCYPN9OQ9T5Y3XEQBBSFTCRXD2NHHG&client_secret=539a25603ae0738757785c022b0d03640b14a6125e&grant_type=refresh_token";

        String creator_token_resp=restTemplate.exchange(creator_token_url, HttpMethod.POST, null, String.class).getBody();
        JSONObject token_response_json = new JSONObject(creator_token_resp);

        String access_token= token_response_json.get("access_token").toString();


        //Payload to write to creator endpoint
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.set("Content-Type","application/json");
        httpHeaders.set("Authorization","Zoho-oauthtoken "+access_token);
        httpHeaders.set("scope","ZohoCreator.form.CREATE");

        //httpHeaders.setContentType(MediaType.APPLICATION_JSON);
        WorkdayResponse workdayResponse = new WorkdayResponse();

        workdayResponse.setWorkdayID(company1);
        workdayResponse.setAddressLine1(addressLine11);
        workdayResponse.setAddressLine2(addressLine21);
        workdayResponse.setCity(city1);
        workdayResponse.setState(state1);
        workdayResponse.setZip(postalCodeFull);
        workdayResponse.setCountyName(countyName);
        workdayResponse.setErrorString(errorString);
        workdayResponse.setErrorString(errorString);
        workdayResponse.setSuggestionList(suggestionList);
        if(errorString.equals("")){
            workdayResponse.setCode("200");
            workdayResponse.setMessage("Address Record Returned");
        }else{
            workdayResponse.setCode("204");
            workdayResponse.setMessage("Bad Address Record Returned");
        }

        HttpEntity<String> httpEntity = new HttpEntity<String>(workdayResponse.toString(),httpHeaders);

        ResponseEntity<String> response = restTemplate.exchange(creatorPostUrl, HttpMethod.POST, httpEntity,String.class);
        ResponseMessage responseMessage = new ResponseMessage();

            responseMessage.setCode(String.valueOf(response.getStatusCodeValue()));
            responseMessage.setMessage(String.valueOf(response.getStatusCode()));

        return responseMessage;
        //return access_token;
    }
    @GetMapping(value ="/aims/v1/oauth",
            // consumes = { MediaType.APPLICATION_XML_VALUE, MediaType.APPLICATION_XML_VALUE },
            produces = { MediaType.APPLICATION_JSON_VALUE}
    )
    public ResponseEntity<String> workDayGetRequest(
            @RequestParam String workday_id,
            @RequestParam String address_type

    )
    {
       String creator_get_url = "https://creator.zoho.com/api/v2/msimpkins/aimscc-connect/report/AIMS_EDQ_Connect_Successful_Records_Report?workday-id="+workday_id+"&address-type="+address_type;
        String creator_token_url="https://accounts.zoho.com/oauth/v2/token?refresh_token=1000.bf9ab0e55d1be092dbf876b5f20deaeb.d12303b316f7f941e7bb06d6bf56204a&client_id=1000.ZCYPN9OQ9T5Y3XEQBBSFTCRXD2NHHG&client_secret=539a25603ae0738757785c022b0d03640b14a6125e&grant_type=refresh_token";

        String creator_token_resp=restTemplate.exchange(creator_token_url, HttpMethod.POST, null, String.class).getBody();
        JSONObject token_response_json = new JSONObject(creator_token_resp);

        String access_token= token_response_json.get("access_token").toString();


        //Payload to write to creator endpoint
        HttpHeaders httpHeaders = new HttpHeaders();
       // httpHeaders.set("Content-Type","application/json");
        httpHeaders.set("Authorization",access_token);
        httpHeaders.set("scope","ZohoCreator.form.CREATE");

        HttpEntity<String> httpEntity = new HttpEntity<String>(null,httpHeaders);

       return restTemplate.exchange(creator_get_url, HttpMethod.GET, httpEntity,String.class);
        //return access_token;
        //ResponseEntity<String> response =
       //if(response.getStatusCode() == OK){
      // }
    }
    /*
     * The endpoint below is for realtime conversion of xml to json
     */
    @PostMapping(value ="/xml-to-json-request-body",
            // consumes = { MediaType.APPLICATION_XML_VALUE, MediaType.APPLICATION_XML_VALUE },
            produces = { MediaType.APPLICATION_JSON_VALUE}
    )
    public @ResponseBody
    String xmlToJsonBody(@RequestBody String xmlValue) throws Exception {
        logger.info("/xml-to-json-request-body");

        String reader=new Scanner(xmlValue).useDelimiter("\\A").next();

        int INDENTATION = 4;
        Scanner s = new Scanner(reader).useDelimiter("\\A");
        // \\A stands for the start of the string
        // \A Matches the beginning of the string.
        //https://stackoverflow.com/questions/577653/difference-between-a-z-and-in-ruby-regular-expressions
        String result = s.hasNext() ? s.next() : "";

        JSONObject jsonObj = XML.toJSONObject(result);
        String json = jsonObj.toString(INDENTATION);

        return json;
       // return xmlValue.toString();
    }
    /*
     * The endpoint below is for realtime conversion of xml to json better working than the previous
     */
    @PostMapping(value ="/xml-to-json-url",
            // consumes = { MediaType.APPLICATION_XML_VALUE, MediaType.APPLICATION_XML_VALUE },
            produces = { MediaType.APPLICATION_JSON_VALUE}
    )
    public @ResponseBody
    String xmlToJsonUrl(@RequestBody XMLToJson xmlToJson) throws Exception {
        logger.info("/xml-to-json-url");
        String requestUrl =xmlToJson.getXml_url().toString();
        // String requestUrl  = "https://www.w3schools.com/xml/cd_catalog.xml";
        URL url = new URL(requestUrl);
        String reader=new Scanner(url.openStream(),"UTF-8").useDelimiter("\\A").next();

        int INDENTATION = 4;
        Scanner s = new Scanner(reader).useDelimiter("\\A");
        // \\A stands for the start of the string
        // \A Matches the beginning of the string.
        //https://stackoverflow.com/questions/577653/difference-between-a-z-and-in-ruby-regular-expressions
        String result = s.hasNext() ? s.next() : "";

        JSONObject jsonObj = XML.toJSONObject(result);
        String json = jsonObj.toString(INDENTATION);

        return json;
    }
    /*
     * The endpoint below is still work in progress, it's for converting xml file to json
     */
    @PostMapping(value = "/xml-to-json-multipart"
            ,produces = { MediaType.APPLICATION_JSON_VALUE}
    )
    public String xmlToJsonMultipart(@RequestParam("file") MultipartFile file) throws IOException {
        logger.info("/xml-to-json-multipart");
        int INDENTATION = 4;
        try {
            Reader reader = new InputStreamReader(file.getInputStream());
            Scanner s = new Scanner(reader).useDelimiter("\\A");
            // \\A stands for the start of the string
            // \A Matches the beginning of the string.
            //https://stackoverflow.com/questions/577653/difference-between-a-z-and-in-ruby-regular-expressions
            String result = s.hasNext() ? s.next() : "";

            JSONObject jsonObj = XML.toJSONObject(result);
            String json = jsonObj.toString(INDENTATION);

             return json;
        } catch (IOException e) {
          // e.printStackTrace();
           return e.getMessage();
        }

    }

    @PostMapping(value = "/unzip_file"
            //, consumes = {MediaType.APPLICATION_XML_VALUE, MediaType.MULTIPART_FORM_DATA_VALUE}
    )
    public ResponseMessage unzipFileAndUpload(@RequestBody UnzipParams unzipParams){
        logger.info("/unzip_file");
        String path_source=unzipParams.getPathSource();
        String path_destination=unzipParams.getPathDestination();
        String host_source=unzipParams.getHostSource();
        String user_source=unzipParams.getUserSource();
        String password_source=unzipParams.getPasswordSource();
        String host_destination=unzipParams.getHostDestination();
        String user_destination=unzipParams.getUserDestination();
        String password_destination=unzipParams.getPasswordDestination();

        //Get the today's date to use it in the zip file name
        DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd");
        Calendar cal = Calendar.getInstance();
        Date date = cal.getTime();
        String todaysdate = dateFormat.format(date);
        System.out.println("Today's date : " + todaysdate);
        todaysdate="2022/09/15";
        String dateFormatted=todaysdate.replace("/", "");
        //System.out.println("Today's date : " + dateFormatted);
        //  JOptionPane.showMessageDialog(null, "Processing File....!",
        // "Hold On!", JOptionPane.INFORMATION_MESSAGE);
        try{
            //this deletes the files already existed in the copy folder
            //File file = new File("C:\\Users\\Befe\\Desktop\\columbia_unzip_copy");
            File file = new File(path_destination);
            String[] fileString=file.list();
            for(String fileS:fileString){
                File file1 = new File(file,fileS);
                file1.delete();
            }
        }catch(Exception ex){

        }
       // Path source = Paths.get("C:\\Users\\Befe\\Desktop\\columbia_unzip\\documentExport_Jenzabar"+dateFormatted+".zip");
       // Path target = Paths.get("C:\\Users\\Befe\\Desktop\\columbia_unzip_copy\\");
         Path source = Paths.get(path_source+"documentExport_Jenzabar"+dateFormatted+".zip");
         Path target = Paths.get(path_destination);
        try {

            unzipFolder(source, target);
          //  System.out.println("Done");

        } catch (IOException e) {
            e.printStackTrace();
        }

        //The code below reads index.txt file and rename the file names for the pdf.

        try {

            File nameChangeFile = new File(path_destination+"index.txt");
            Scanner scan = new Scanner(nameChangeFile);
            int i=1;
            while(scan.hasNextLine()){

                String scannedLine=scan.nextLine();
                if(i > 1){
                    String[] arrayOfScannedLines=scannedLine.split(",",-1);

                    String pdfString=arrayOfScannedLines[5];
                    String janzebarId=arrayOfScannedLines[0].replace("\"", "");
                    String first_name=arrayOfScannedLines[1].replace("\"", "");
                    String last_name=arrayOfScannedLines[2].replace("\"", "");
                    String material_key=arrayOfScannedLines[8].replace("\"", "").replace(":", "_");
                    String material_date=arrayOfScannedLines[10].replace("\"", "").replace(":", "_");

                    String old_file_name_formatted=pdfString.substring(1,pdfString.length()-5);
                    String new_file_name=janzebarId+"_"+first_name+"_"+last_name+"_"+material_key+"_"+old_file_name_formatted+".pdf";
                    String file_name1=path_destination+old_file_name_formatted+".pdf";

                    String file_name2=path_destination+new_file_name;

                    File file_orginal = new File(file_name1);
                    File file_renamed = new File(file_name2);

                   // file_orginal.renameTo(file_renamed);
                    if(!file_renamed.exists()){
                        file_orginal.renameTo(file_renamed);
                    }else{

                    }
                   /*
                    String file_name3=path_destination+new_file_name;

                    File file_orginal = new File(file_name1);
                    File file_renamed = new File(file_name2);

                    if(!file_renamed.exists()){
                        file_orginal.renameTo(file_renamed);
                    }
                    int j=1;
                    while (file_renamed.exists() && !file_renamed.isDirectory()) {
                        file_orginal.renameTo(new File(j + "-" + file_name3));
                        j++;
                    }
                    */

                }
                i++;
            }
            scan.close();
        } catch (FileNotFoundException ex) {
            ex.printStackTrace();
        } catch (IOException ex) {
            ex.printStackTrace();
        }

     //   Map<String, Object> bodyParamMap = new HashMap<>();

      //  bodyParamMap.put("code", "200");
     //when the renameing is finalized delete the index.txt file
        try{
            //this deletes the files already existed in the copy folder
            //File file = new File("C:\\Users\\Befe\\Desktop\\columbia_unzip_copy");
            File file = new File(path_destination);
              File file2 = new File(file,"index.txt");
                file2.delete();

        }catch(Exception ex){

        }
        ResponseMessage responseMessage = new ResponseMessage();
            responseMessage.setCode("200");
            responseMessage.setMessage("File Unzipped");

            return responseMessage;
    }
    public static void unzipFolder(Path source, Path target) throws IOException {

        try (ZipInputStream zis = new ZipInputStream(new FileInputStream(source.toFile()))) {

            // list files in zip
            ZipEntry zipEntry = zis.getNextEntry();

            while (zipEntry != null) {

                boolean isDirectory = false;

                if (zipEntry.getName().endsWith(File.separator)) {
                    isDirectory = true;
                }

                Path newPath = zipSlipProtect(zipEntry, target);

                if (isDirectory) {
                    Files.createDirectories(newPath);
                } else {

                    if (newPath.getParent() != null) {
                        if (Files.notExists(newPath.getParent())) {
                            Files.createDirectories(newPath.getParent());
                        }
                    }

                    // copy files, nio
                    Files.copy(zis, newPath, StandardCopyOption.REPLACE_EXISTING);
                }
                zipEntry = zis.getNextEntry();
            }
            zis.closeEntry();
            zis.close();
        }
    }
    // protect zip slip attack
    public static Path zipSlipProtect(ZipEntry zipEntry, Path targetDir)
            throws IOException {

        // test zip slip vulnerability
        // Path targetDirResolved = targetDir.resolve("../../" + zipEntry.getName());

        Path targetDirResolved = targetDir.resolve(zipEntry.getName());

        // make sure normalized file still has targetDir as its prefix
        // else throws exception
        Path normalizePath = targetDirResolved.normalize();
        if (!normalizePath.startsWith(targetDir)) {
            throw new IOException("Bad zip entry: " + zipEntry.getName());
        }
        return normalizePath;
    }
}
