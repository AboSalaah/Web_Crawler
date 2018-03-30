package Crawler;

import java.io.IOException;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Scanner;

public class NormalizeURL {

    public static void main(String []args) throws MalformedURLException {

        String str = "http://208.77.188.166///display?lang=en&article=fred";
        String urlNormalized = null;
        try {
            urlNormalized = normalize(str);
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
        System.out.println("urlNormalized : " + urlNormalized);
    }

    public static String normalize(String str) throws MalformedURLException, UnknownHostException {
        URL url;

        StringBuilder ans = new StringBuilder();
        url = new URL(str);

        System.out.println("Query : " + url.getQuery());
        System.out.println("Authority : " + url.getAuthority());
        System.out.println("Port : " + url.getPort());
        System.out.println("Host : " + url.getHost());
        System.out.println("protocol : " + url.getProtocol());
        System.out.println("DefaultPort : " + url.getDefaultPort());
        System.out.println("getPath : " + url.getPath());
        System.out.println("UserInfo : " + url.getUserInfo());
        System.out.println("file : " + url.getFile());

        /*Converting the scheme and host to lower case*/
        if(url.getProtocol().toLowerCase().equals("https")){
            ans.append("http");
        }else {
            ans.append(url.getProtocol().toLowerCase());
        }
        ans.append("://");
        String authority = url.getAuthority();
        StringBuilder authorityBuilder = new StringBuilder();
        if(!checkIP(authority)) {
            for (int i = (authority.substring(0, 4).equals("www.") ? 4 : 0); i < authority.length(); i++) {
                if (authority.charAt(i) >= 'A' && authority.charAt(i) <= 'Z') {
                    authorityBuilder.append(authority.substring(i, i + 1).toLowerCase());
                } else {
                    if (authority.charAt(i) == ':') {
                        /* remove default port */
                        int port = url.getPort();
                        if (port != url.getDefaultPort()) {
                            authorityBuilder.append(":");
                            authorityBuilder.append(Integer.toString(url.getPort()));
                        }
                        break;
                    } else {
                        authorityBuilder.append(authority.charAt(i));
                    }
                }
            }
            ans.append(authorityBuilder.toString());
        } else {

            /* Replacing IP with domain name. */
            ans.append(InetAddress.getByName(authority).getHostName());
            System.out.println("Host Name : " + InetAddress.getByName(authority).getHostName());
        }

        /* -------------------------------------------------------------------------*/

        /* Capitalizing letters in escape sequences */
        String path = url.getPath();
        StringBuilder pathBuilder = new StringBuilder();
        for (int i = 0; i < path.length(); i++){
            if(path.charAt(i) == '%' && i + 1 < path.length()){
                if(path.charAt(i+1) <= 'z' && path.charAt(i+1) >= 'a'){
                    pathBuilder.append("%");
                    pathBuilder.append(path.substring(i+1,i+2).toUpperCase());
                    i++;
                }else{
                    pathBuilder.append(path.charAt(i));
                }
            }else if(path.charAt(i) == '/' && i + 1 < path.length() ){
                pathBuilder.append(path.charAt(i));
                i++;
                while (i < path.length() && path.charAt(i) == '/'){
                    i++;
                }
                if(i < path.length())
                pathBuilder.append(path.charAt(i));
            }else {
                pathBuilder.append(path.charAt(i));
            }
        }
        /* Adding trailing */
        if(path.charAt(path.length()-1) != '/' && (url.getQuery() == null || url.getQuery().isEmpty())){
           pathBuilder.append("/");
        }
        ans.append(pathBuilder.toString());

        /* -------------------------------------------------------------------------*/

        /* Sorting the query parameters */
        String query = url.getQuery();
        StringBuilder queryBuilder = new StringBuilder();

        if(query != null && !query.isEmpty()) {

            List<String> queryParameters = new ArrayList<String>();
            String tmp = "";
            int numPar = 0;
            for (int i = 0; i < query.length(); i++) {
                if(query.charAt(i) != '&')tmp += query.charAt(i);
                else{
                    queryParameters.add(tmp);
                    numPar++;
                    tmp = "";
                }
            }
            queryParameters.add(tmp);
            numPar++;
            Collections.sort(queryParameters);
            queryBuilder.append("?");
            for (int i =0; i < numPar; i++) {
                queryBuilder.append(queryParameters.get(i));
                if(i!=numPar-1){
                    queryBuilder.append("&");
                }
            }
        }
        ans.append(queryBuilder.toString());

        return ans.toString();

    }

    /* check if the authority is IP or HostName */
    private static boolean checkIP(String authority) {

        System.out.println("Checking IP ...");
        System.out.println(authority);
        for (int i = 0; i < authority.length(); i++){
            if(!(authority.charAt(i) >= '0' && authority.charAt(i) <= '9') && authority.charAt(i) != '.'){
                System.out.println("return false");
                return false;

            }
        }
        return true;
    }
}