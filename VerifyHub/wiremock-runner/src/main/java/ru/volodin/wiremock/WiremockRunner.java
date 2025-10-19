package ru.volodin.wiremock;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;

public class WiremockRunner {
    public static void main(String[] args) {
        String filesRoot = "VerifyHub/wiremock";

        WireMockServer server = new WireMockServer(
                WireMockConfiguration.options()
                        .port(9090)
                        .usingFilesUnderDirectory(filesRoot)
        );

        server.start();
        System.out.printf("WireMock started on http://localhost:9090 (root: %s)%n", filesRoot);

        Runtime.getRuntime().addShutdownHook(new Thread(server::stop));
    }
}
