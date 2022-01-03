import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Pixiv {
    public static void main(String[] args) throws IOException, InterruptedException {
        //region 代理设置
        //代理地址和端口 todo 换成你的代理
        String proxyHost = "127.0.0.1";
        String proxyPort = "7890";
        //http开启代理
        System.setProperty("http.proxyHost", proxyHost);
        System.setProperty("http.proxyPort", proxyPort);
        // 对https也开启代理
        System.setProperty("https.proxyHost", proxyHost);
        System.setProperty("https.proxyPort", proxyPort);
        //endregion

        //region 通过wiki获取干员名称，生成搜索关键字 ["明日方舟 (嵯峨 OR Saga OR サガ)"]
        Document wikiDoc = Jsoup.connect("https://prts.wiki/w/CHAR?filter=,xwAAAAggAAAAAAAAAAAAAAAAAAAAAA").get();
        Elements characters = wikiDoc.select(".smwdata");
        List<String> characters_searchKeywords = new ArrayList<>();
        for (Element character : characters) {
            String cn = character.attr("data-cn");
            String en = character.attr("data-en");
            if(!en.equals("")) en="OR "+en;
            String jp = character.attr("data-jp");
            if(!jp.equals("")) jp="OR "+jp;

            String searchKeyword = String.format("明日方舟 (%s %s %s)", cn, en, jp);
            characters_searchKeywords.add(searchKeyword);
        }
        //endregion

        //region 通过搜索关键字，循环请求p站 获取涩图total
        List<Map<String, String>> resList = new ArrayList<>();
        int count = 0;
        for (String keyWords : characters_searchKeywords) {
            count++;
            String url = String.format("https://www.pixiv.net/ajax/search/artworks/%s?word=%s&order=date_d&mode=r18&p=1&s_mode=s_tag&type=all&lang=zh_tw", keyWords, keyWords);
            OkHttpClient client = new OkHttpClient().newBuilder()
                    .build();
            Request request = new Request.Builder()
                    .url(url)
                    .method("GET", null)
                    .addHeader("sec-ch-ua", "\" Not A;Brand\";v=\"99\", \"Chromium\";v=\"96\", \"Google Chrome\";v=\"96\"")
                    .addHeader("accept", "application/json")
                    .addHeader("sec-ch-ua-mobile", "?0")
                    .addHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/96.0.4664.110 Safari/537.36")
                    .addHeader("x-user-id", "xxx")// todo 换成你的id
                    .addHeader("sec-ch-ua-platform", "\"Windows\"")
                    .addHeader("Sec-Fetch-Site", "same-origin")
                    .addHeader("Sec-Fetch-Mode", "cors")
                    .addHeader("Sec-Fetch-Dest", "empty")
                    .addHeader("Cookie", "xxx")// todo 换成你的 Cookie
                    .build();
            Response response = client.newCall(request).execute();


            Map<String, String> resMap = new HashMap<>();
            //获取总数
            String rgex = "\"total\":(.*?),";
            resMap.put("total", getSubUtilSimple(response.body().string(), rgex));
            //顺便设置下搜索词keywords
            resMap.put("keyWords", keyWords);

            resList.add(resMap);

            //打印进度
            System.out.println("第" + count + "次数："+keyWords+"--"+resMap.get("total"));
            //伪装用户操作
            Thread.currentThread().sleep((long) (Math.random() * 2000));

        }
        //endregion

        //region 排序
        Collections.sort(resList, new Comparator<Map>() {
            @Override
            public int compare(Map o1, Map o2) {
                return Integer.parseInt((String) o2.get("total")) - Integer.parseInt((String) o1.get("total"));
            }
        });
        //endregion

        //打印结果
        for (Map s : resList) {
            System.out.println(s.toString());
        }
    }

    public static String getSubUtilSimple(String soap, String rgex) {
        Pattern pattern = Pattern.compile(rgex);
        Matcher m = pattern.matcher(soap);
        while (m.find()) {
            return m.group(1);
        }
        return "";
    }
}
