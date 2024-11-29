package it.unibo.ai.didattica.competition.tablut.client.smart;

import java.io.IOException;

public class TablutSmartWhiteClient {
    public static void main(String[] args) throws IOException {
        TablutSmartClient client = new TablutSmartClient("white", "AIPlayer1");
        client.run();
    }
}
