package com.example;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.net.Authenticator;
import java.net.PasswordAuthentication;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;


public class CraigslistCrawler {

	private static final String CRAIGS_QUERY_URL = "https://sfbay.craigslist.org/d/apts-housing-for-rent/search/apa";
	private static final String USER_AGENT = "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_11_3) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/55.0.2883.95 Safari/537.36";
	private int index = 0;
	private List<String> proxyList;
	private final String authUser = "bittiger";
    private final String authPassword = "cs504";

	private void setProxy() {
        //rotate, round robbin
        if (index == proxyList.size()) {
            index = 0;
        }
        String proxy = proxyList.get(index);
        System.setProperty("socksProxyHost", proxy); // set proxy server
        index++;
    }
	
	public CraigslistCrawler(String proxy_file) {
		initProxyList(proxy_file);    
	}
	
	private void initProxyList(String proxy_file) {
        proxyList = new ArrayList<String>();
        try (BufferedReader br = new BufferedReader(new FileReader(proxy_file))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] fields = line.split(",");
                String ip = fields[0].trim();
                proxyList.add(ip);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        Authenticator.setDefault(
                new Authenticator() {
                    @Override
                    public PasswordAuthentication getPasswordAuthentication() {
                        return new PasswordAuthentication(
                                authUser, authPassword.toCharArray());
                    }
                }
        );
        System.setProperty("http.proxyUser", authUser);
        System.setProperty("http.proxyPassword", authPassword);
        System.setProperty("socksProxyPort", "61336"); // set proxy port
    }
	

	public void Crawl(String url) {		
        try {           
            setProxy();
            System.out.println("request_url = " + url);            
            HashMap<String,String> headers = new HashMap<String,String>();
            headers.put("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8");
            headers.put("Accept-Language", "en-US,en;q=0.8");            
            Document doc = Jsoup.connect(url).headers(headers).userAgent(USER_AGENT).timeout(100000).get();
            Elements items = doc.select("li[data-pid]");            
            if (items.size() == 0) {
            	System.out.println("0 result for url :" + url);  
            	return;
            }            
            //Elements prods = doc.select("a[title][href]");
            System.out.println("num of results from dom = " + items.size());
            int id = 0;
            for(Element item : items) {                               
                Element detail_url = item.select("a[href]").first();
                String str_url = "";
                if(detail_url != null)
                	str_url = detail_url.attr("href");
                Element title = item.select("a.result-title").first();
                String str_title = "";
                if(title != null)
                	str_title = title.text();
                Element price = item.select("span.result-price").first();  
                String str_price = "";
                if(price != null)
                	str_price = price.text().replace("$", "");
                Element hood = item.select("span.result-hood").first();                
                String str_hood = "";
                if(hood != null)
                	str_hood = hood.text();                               
                System.out.println("ID: " + id +" ,Title: " + str_title + ", Price: " + str_price + ", url: " + str_url + ", hood: " + str_hood);                
                id++;
            }
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
	}
	
	public static void main(String[] args) throws IOException {               
        CraigslistCrawler crawler = new CraigslistCrawler("./proxylist_bittiger.csv");        
        crawler.Crawl(CRAIGS_QUERY_URL);       
    }
}
