package Crawler;

import org.jsoup.HttpStatusException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ConnectException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.*;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Web_Crawling implements Runnable {
    private static final int HTML_TAGS_NUMBER = 7;
    // priorities of html tags
    private static final int H1_PRIORITY = 7;
    private static final int H2_PRIORITY = 6;
    private static final int H3_PRIORITY = 5;
    private static final int H4_PRIORITY = 4;
    private static final int H5_PRIORITY = 3;
    private static final int H6_PRIORITY = 2;
    private static final int P_PRIORITY = 1;
    // html tags
    private static final String HEADER1 = "h1";
    private static final String HEADER2 = "h2";
    private static final String HEADER3 = "h3";
    private static final String HEADER4 = "h4";
    private static final String HEADER5 = "h5";
    private static final String HEADER6 = "h6";
    private static final String PARAGRAPH = "p";
    private static final String ANCHOR = "a";

    private static Map<String, Integer> tagPriorities = new HashMap<>();
    // private final Web_Crawler web_crawler;
    private static Object object1 = new Object();
    private static Object object2 = new Object();
    private static Object object3 = new Object();
    //private static BlockingQueue<String> current_urls = new LinkedBlockingQueue<>();
    private static BlockingQueue<String> final_urls = new LinkedBlockingQueue<>();
    private static String urlPattern = "^http(s{0,1})://[a-zA-Z0-9_/\\-\\.]+\\.([A-Za-z/]{2,5})[a-zA-Z0-9_/\\&\\?\\=\\-\\.\\~\\%]*";
    private static Map<String, Boolean> robots_done = Collections.synchronizedMap(new HashMap<>());
    public static final char[] special_chars = {'<', '(', '[', '{', '^', '-', '=', '!', '|', ']', '}', ')', '?', '+', '.', '>'};
    public static final String robots_or_not = "User-agent: (.*)|Disallow: (.*)|Allow: (.*)";
    private static AtomicInteger to_crawl = new AtomicInteger();
    // de ely shayla le kol url el 7agat bt3to
    private static Map<String, Url_Data> visited = Collections.synchronizedMap(new HashMap<>());
    private static dbConnector db;
    private static List<String> current = Collections.synchronizedList(new ArrayList<String>());
    //private static ArrayList<String> current=new ArrayList<>();
    // private static ArrayList<String>next = new ArrayList<>();
    private static List<String> next = Collections.synchronizedList(new ArrayList<String>());
    // da elly lw l2a 3dd el words a2l mno m4 ha update
    private static final int NW = 20;
    @Override
    public void run() {
        System.out.println(Thread.currentThread().getName() + " Started");
        while (true) {


            String url;
            Document doc;
            try {
                synchronized (object1) {
                    if (to_crawl.get() <= 0) break;
                    if (current.size() <= 0 && next.size() <= 0) continue;
                    if (current.size() <= 0) {
                        for (int i = 0; i < next.size(); ++i) {
                            current.add(next.get(i));
                        }

                        next.clear();
                    }
                    url = current.remove(new Random().nextInt(current.size()));
                    //if (final_urls.size() >= 20)break;

                }
                if (to_crawl.get() <= 0) break;
                doc = Jsoup.connect(url)
                        .timeout(7000)
                        .get();

                //final_urls.put(url);
                //System.out.println("to_crawlllllllllll: "+to_crawl.get());
                //to_crawl.decrementAndGet();
                //db.updateCrawlReminder(to_crawl);


                /*
                new tgrbaaa
                 */
                Elements elements = doc.body().select("*");
                ArrayList<String> links = new ArrayList<>();
                Url_Data url_data = new Url_Data();
                int numOfWords = 0;
                for (Element element : elements) {
                    if (element.tag().toString() == ANCHOR) {
                        links.add(element.attr("abs:href"));
                    } else {
                        if (!checkTagExist(element.tagName())) continue;
                        String text = element.ownText();
                        if (text.isEmpty()) continue;
                        String alphaOnly = text.replaceAll("^[\\u0600-\\u065F\\u066A-\\u06EF\\u06FA-\\u06FFa-zA-Z]+[\\u0600-\\u065F\\u066A-\\u06EF\\u06FA-\\u06FFa-zA-Z-_]*$", " ");
                        alphaOnly = alphaOnly.trim();
                        numOfWords += alphaOnly.split(" ").length;
                        url_data.addText(alphaOnly);
                        url_data.addPriority(tagPriorities.get(element.tagName()));
                    }
                }
                ArrayList<ArrayList<String>> allowed_and_disallowd = robots_parser(url);

                synchronized (object2) {
                    if (to_crawl.get() <= 0) break;
                    visited.put(url, url_data);
                    if (numOfWords>= NW)
                    {
                        db.updateDocument(url, url_data);
                        final_urls.put(url);
                        to_crawl.decrementAndGet();
                        db.updateCrawlReminder(to_crawl);
                    }

                    for (String link : links) {
                        if (link.length() == 0) continue;
                        if (!link.matches(urlPattern)) continue;
                        Pattern p;
                        boolean dis = false;
                        boolean allow = false;
                        // 3shan a5ls mn el forward slash ely btb2a fel a5er 3shan kanet bt3ml moshkla fel match lw ana bdwr 3la 7aga tkon fe a5er el url
                        StringBuilder temp = new StringBuilder(link);
                        if (temp.charAt(temp.length() - 1) == '/') temp.deleteCharAt(temp.length() - 1);
                        String disallowed = "";
                        String allowed = "";
                        for (int i = 0; i < 2; ++i) {
                            for (int j = 0; j < allowed_and_disallowd.get(i).size(); ++j) {
                                p = Pattern.compile(allowed_and_disallowd.get(i).get(j));
                                Matcher m = p.matcher(temp);
                                if (m.find()) {
                                    if ((i % 2) == 0) {
                                        dis = true;
                                        disallowed = allowed_and_disallowd.get(i).get(j);
                                    } else {
                                        allow = true;
                                        allowed = allowed_and_disallowd.get(i).get(j);
                                    }
                                }
                            }
                        }

                        if (!visited.containsKey(link)) {
                            if (!dis || (allow && (allowed.length() >= disallowed.length()))) {
                                visited.put(link, new Url_Data());
                                next.add(link);
                                db.insertDocument(link);
                            }

                        }
                    }
                }


            } catch (UnknownHostException e) {
                System.err.println("Unknown host");

            } catch (SocketTimeoutException e) {
                System.err.println("IP cannot be reached");

            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }


        }

        System.out.println(Thread.currentThread().getName() + " Finished");

    }


    private ArrayList<ArrayList<String>> robots_parser(String url) {
        ArrayList<ArrayList<String>> allowed_and_disallowed = new ArrayList<>();
        allowed_and_disallowed.add(new ArrayList<>());
        allowed_and_disallowed.add(new ArrayList<>());
        Pattern p = Pattern.compile("^(http(s?)://([^/]+))");
        Matcher m = p.matcher(url);
        if (m.find()) {
            String base_url = m.group(1);
            ArrayList<String> robots_lines = new ArrayList<>();
            ArrayList<String> disallow = new ArrayList<>();
            ArrayList<String> allow = new ArrayList<>();
            synchronized (object3) {
                if (robots_done.containsKey(base_url))
                    return allowed_and_disallowed;
                //put the url into the map to avoid reading the same robots again
                robots_done.put(base_url, true);
            }
            try (BufferedReader in = new BufferedReader(
                    new InputStreamReader(new URL(base_url + "/robots.txt").openStream()))) {
                String line = null;
                boolean not_robot = true;
                Pattern pattern = Pattern.compile(robots_or_not);
                Matcher matcher;
                while ((line = in.readLine()) != null) {
                    matcher = pattern.matcher(line);
                    if (matcher.find()) not_robot = false;
                    robots_lines.add(line);
                }
                if (!not_robot) {
                    boolean dis = false;
                    boolean my_user_agent = false;
                    for (String robots_line : robots_lines) {
                        int idx = robots_line.indexOf(":");
                        if (idx + 2 < robots_line.length()) {
                            String s = robots_line.substring(robots_line.indexOf(":") + 2);
                            if (robots_line.charAt(0) == 'U') {
                                if (dis) {
                                    my_user_agent = false;
                                    dis = false;
                                }
                                my_user_agent |= s.equals("*");
                            } else if ((robots_line.charAt(0) == 'D' || robots_line.charAt(0) == 'A') && my_user_agent == true) {
                                dis = true;
                                String modefied_url = "";
                                for (int i = 0; i < s.length(); ++i) {
                                    if (s.charAt(i) == '*') {
                                        if (s.charAt(i) == '*') modefied_url += ".*";
                                    } else {
                                        String temp = String.valueOf(s.charAt(i));
                                        for (int j = 0; j < 16; ++j) {
                                            if (s.charAt(i) == special_chars[j]) {
                                                temp = "\\" + s.charAt(i);
                                                break;
                                            }
                                        }
                                        if (s.charAt(i) == '\\') temp = "\\\\";
                                        modefied_url += temp;
                                    }
                                }
                                if (robots_line.charAt(0) == 'D') disallow.add(base_url + modefied_url);
                                else allow.add(base_url + modefied_url);
                            }

                        }

                    }

                    allowed_and_disallowed.set(0, disallow);
                    allowed_and_disallowed.set(1, allow);
                    return allowed_and_disallowed;

                }
            } catch (IOException e) {
                System.err.println("there's no robots.txt in this website");
                e.printStackTrace();
            }

        }
        return allowed_and_disallowed;

    }

    public static void setCurrent_urls(ArrayList<String> urls) throws InterruptedException {
        for (String url : urls) {
            // current_urls.put(url);
            current.add(url);
            db.insertDocument(url);
        }
    }

    public static void setVisited(Map<String, Url_Data> visitedd) {
        visited = visitedd;
    }

    public static BlockingQueue<String> getFinal_urls() {
        return final_urls;
    }

    public static void setTo_crawl(int to_crawll) {
        to_crawl.set(to_crawll);
    }

    public static void setDBObject(dbConnector dbb) {
        db = dbb;
    }

    public static AtomicInteger getTo_crawl() {
        return to_crawl;
    }

    public static void setTagPriorities() {
        tagPriorities.put(HEADER1, H1_PRIORITY);
        tagPriorities.put(HEADER2, H2_PRIORITY);
        tagPriorities.put(HEADER3, H3_PRIORITY);
        tagPriorities.put(HEADER4, H4_PRIORITY);
        tagPriorities.put(HEADER5, H5_PRIORITY);
        tagPriorities.put(HEADER6, H6_PRIORITY);
        tagPriorities.put(PARAGRAPH, P_PRIORITY);
    }

    public static boolean checkTagExist(String s) {
        return s.equals(HEADER1) || s.equals(HEADER2) || s.equals(HEADER3) || s.equals(HEADER4) || s.equals(HEADER5) || s.equals(HEADER6) || s.equals(PARAGRAPH);
    }
}
