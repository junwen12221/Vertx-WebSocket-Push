import io.vertx.core.Vertx;
import io.vertx.core.http.*;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.StaticHandler;

import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

import static java.lang.System.out;

/**
 * Created by karak on 16-11-9.1
 * Vertx应用开发实例教程的WebSocket例子
 */
public class Server {
    public static void main(String[] args) {
        Vertx vertx = Vertx.vertx();
        HttpServer server = vertx.createHttpServer();
        HttpClient client = vertx.createHttpClient(new HttpClientOptions().setDefaultPort(80).setDefaultHost("weatherapi.market.xiaomi.com"));
        AtomicLong timerID = new AtomicLong(0L);
        AtomicReference<String> citycode = new AtomicReference<>("");
        final Router router = Router.router(vertx);
        router.route().handler(StaticHandler.create().setCachingEnabled(false));
        server.requestHandler(router::accept);
        server.websocketHandler((websocket) -> websocket.handler((buffer) -> {
            citycode.set(buffer.getString(0, buffer.length()));
            if (timerID.get() != 0) {
                vertx.cancelTimer(timerID.get());
            }
            if (!"".equals(citycode.get())) {
                timerID.set(vertx.setPeriodic(10000, (i) -> {
                    out.println("定时器工作...");
                    HttpClientRequest requset = client.request(HttpMethod.GET, "/wtr-v2/weather?cityId=" + citycode, (resp) -> {
                        resp.bodyHandler((body) -> {
                            out.println("接收天气数据: " + body.length());
                            String str = body.getString(0, body.length(), "utf-8");
                            out.println(str);
                            websocket.writeFinalTextFrame(str);
                        });
                    });
                    requset.headers().set("contentType", "text/html;charset=UTF-8");
                    requset.end();
                }));
            }
        }));
        server.listen(8181,"127.0.0.1");
    }
}
