package it.unibo.ai.didattica.competition.tablut.client.smart;

import java.io.IOException;

public class TablutSmartBlackClient {
    public static void main(String[] args) throws IOException {
        TablutSmartClient client = new TablutSmartClient("black", "AIPlayer2");
        client.run();
    }
}
