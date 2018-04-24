package Crawler;

import com.mongodb.client.FindIterable;
import org.bson.Document;

import javax.print.Doc;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Main {
    private static final int Thread_count = 4;
    static  dbConnector db = new dbConnector();
    public static void main(String[] args) throws IOException {
        Web_Crawling.setTagPriorities();
        Web_Crawling.setDBObject(db);
        int kam_crawl=0;
        while (true) {
            System.out.println("ana fel crawlaayaa: "+(++kam_crawl));
            System.out.println("to_crawl "+Web_Crawling.getTo_crawl().get());
            long startTime = System.currentTimeMillis();
            ArrayList<String> urls = new ArrayList<>();

            //db.clean();
            // db.insertDocument("https://www.google.com.eg");
            // break;

            FindIterable<Document> iterDocs = db.getAllDocuments();
            // Getting the iterator
            //Iterator it = iterDocs.iterator();
            //split the documents
            ArrayList<String> to_crawl_urls = new ArrayList<>();
            Map<String, Url_Data> visited = Collections.synchronizedMap(new HashMap<>());
            for (Document iterDoc : iterDocs) {
                String url = iterDoc.getString("url");
                if (iterDoc.getInteger("crawled") == 1) {
                    Url_Data url_data = new Url_Data();
                    url_data.addText((ArrayList<String>) iterDoc.get("url_data"));
                    url_data.addTag((ArrayList<Integer>) iterDoc.get("tags"));
                    visited.put(url, url_data);
                } else {
                    to_crawl_urls.add(url);
                }

            }
            if (!to_crawl_urls.isEmpty()) {
                //set the current queue and visited map
                try {
                    Web_Crawling.setCurrent_urls(to_crawl_urls);
                    Web_Crawling.setVisited(visited);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                ArrayList<Thread> threads = new ArrayList<>();

                for (int i = 0; i < Thread_count; ++i) {
                    threads.add(new Thread(new Web_Crawling()));
                }
                for (int i = 0; i < Thread_count; ++i) threads.get(i).start();
                for (int i = 0; i < Thread_count; ++i) {
                    try {
                        threads.get(i).join();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
          BlockingQueue<String> arr=Web_Crawling.getFinal_urls();
            System.out.println("final size: "+arr.size());
            Set<String>seet=new HashSet<>();
            for(String arrr: arr){
                seet.add(arrr);
            }
            System.out.println("unique: "+seet.size());
            db.printDocs();
            db.removeOverhead();


            //reset the ones with high priority
            db.sort_and_update();
            System.out.println("able el set bta3 to_crawl: "+Web_Crawling.getTo_crawl().get());
            db.setRecrawl();
            System.out.println("b3d el set bta3 to_crawl: "+Web_Crawling.getTo_crawl().get());

            System.out.println(db.printDocs());
            System.out.println(db.getCrawlReminder());
            pageRank();

            // pri(x) = lamda * 1/database(linkat) +(1-lamda) * sigma (3la in el x) pr(i-1)(y)/out(y)



            //break;

        }



    }
    private static  void pageRank(){
        // pri(x) = lamda * 1/database(linkat) +(1-lamda) * sigma (3la in el x) pr(i-1)(y)/out(y)
        double lamda = 0.3;
        int iterations=10;
        FindIterable<Document> allDocuments = db.getAllDocuments();
        Iterator it = allDocuments.iterator();
        int cnt=0;
        while (it.hasNext()) {
            ++cnt;
        }
        double previousPageRanks []= new double[cnt];
        double currentPageRanks[] = new double[cnt];
        ArrayList<ArrayList<String> > In = new ArrayList< >();
        ArrayList<Integer> out = new ArrayList< >();
        ArrayList<String> revIdx = new ArrayList< >();
        HashMap<String,Integer> urlIdx = new HashMap<>();
        int i = 0;
        for(Document doc : allDocuments){
            previousPageRanks[i]=1.0/cnt;
            currentPageRanks [i]=previousPageRanks[i];
            In.add((ArrayList<String>)doc.get("in"));
            out.add(((ArrayList<String>)doc.get("out")).size());
            urlIdx.put(doc.getString("url") , i++);
            revIdx.add(doc.getString("url"));
        }
        for(int a=0;a<iterations;++a){

            for(int j = 0 ; j<cnt ; j++){
                currentPageRanks[j]=lamda*(1.0/cnt);
                for(int k = 0 ; k<In.get(j).size() ; k++)
                {
                    String cur = In.get(j).get(k);
                    int idx = urlIdx.get(cur);
                    currentPageRanks[j] += previousPageRanks[idx]/out.get(idx);
                }
            }
            for(int j = 0 ; j<cnt ; j++){
                previousPageRanks[j] = currentPageRanks[j];
            }
        }
        db.updatePageRank(currentPageRanks , revIdx);
































    }
}