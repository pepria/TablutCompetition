package it.unibo.ai.didattica.competition.tablut.client.smart;

import it.unibo.ai.didattica.competition.tablut.client.TablutClient;
import it.unibo.ai.didattica.competition.tablut.domain.*;
import it.unibo.ai.didattica.competition.tablut.domain.State.Turn;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

public class TablutSmartClient extends TablutClient {

    public static void main(String[] args) throws UnknownHostException, IOException, ClassNotFoundException {
        int gametype = 4;
        String role = "";
        String name = "smart";
        String ipAddress = "localhost";
        int timeout = 60;
        if (args.length < 1) {
            System.out.println("You must specify which player you are (WHITE or BLACK)");
            System.exit(-1);
        } else {
            System.out.println(args[0]);
            role = (args[0]);
        }
        if (args.length == 2) {
            System.out.println(args[1]);
            timeout = Integer.parseInt(args[1]);
        }
        if (args.length == 3) {
            ipAddress = args[2];
        }
        System.out.println("Selected client: " + args[0]);

        TablutSmartClient client = new TablutSmartClient(role, name, gametype, timeout, ipAddress);
        client.run();
    }

    private int game = 4;
    private static final int MAX_DEPTH = 3;  // Minimax�������

    public TablutSmartClient(String player, String name, int game, int timeout, String ipAddress) throws IOException {
        super(player, name, timeout, ipAddress);
        this.game = game;
    }
    public TablutSmartClient(String player, String name) throws IOException {
        super(player, name);
    }

    // ������Ѷ���
    public Action bestMove(State state, Game game, State currentState) {
        List<Action> possibleMoves = generateLegalMoves(currentState, this.getPlayer());
        possibleMoves = possibleMoves.stream().filter(one -> {
            try {
                game.checkMove(currentState, one);
                return true;
            } catch (Exception e) {
                return false;
            }
        }).collect(Collectors.toList());
        if (possibleMoves.isEmpty()) {
            // todo
            Action extracted = this.extracted(game);
            possibleMoves.add(extracted);
        }else{
            System.out.println(possibleMoves.size() + " possible moves found");;
        }

        int bestValue = Integer.MIN_VALUE;
        Action bestMove = possibleMoves.get(0);

        // ��ÿ���Ϸ�������������
        for (Action move : possibleMoves) {
            State newState = applyMove(currentState, move);
            int moveValue = minimax(newState, MAX_DEPTH, Integer.MIN_VALUE, Integer.MAX_VALUE, false);
            if (moveValue > bestValue) {
                bestValue = moveValue;
                bestMove = move;
            }
        }

        return bestMove;
    }



    private Action extracted(Game rules) {
        State state;
        List<int[]> pawns = new ArrayList<int[]>();
        List<int[]> empty = new ArrayList<int[]>();

        System.out.println("You are player " + this.getPlayer().toString() + "!");

        while (true) {
            System.out.println("Current state:");
            state = this.getCurrentState();
            System.out.println(state.toString());
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
            }

            if (this.getPlayer().equals(Turn.WHITE)) {
                // Mio turno
                if (this.getCurrentState().getTurn().equals(Turn.WHITE)) {
                    int[] buf;
                    for (int i = 0; i < state.getBoard().length; i++) {
                        for (int j = 0; j < state.getBoard().length; j++) {
                            if (state.getPawn(i, j).equalsPawn(State.Pawn.WHITE.toString())
                                    || state.getPawn(i, j).equalsPawn(State.Pawn.KING.toString())) {
                                buf = new int[2];
                                buf[0] = i;
                                buf[1] = j;
                                pawns.add(buf);
                            } else if (state.getPawn(i, j).equalsPawn(State.Pawn.EMPTY.toString())) {
                                buf = new int[2];
                                buf[0] = i;
                                buf[1] = j;
                                empty.add(buf);
                            }
                        }
                    }

                    int[] selected = null;

                    boolean found = false;
                    Action a = null;
                    try {
                        a = new Action("z0", "z0", Turn.WHITE);
                    } catch (IOException e1) {
                        // TODO Auto-generated catch block
                        e1.printStackTrace();
                    }
                    while (!found) {
                        if (pawns.size() > 1) {
                            selected = pawns.get(new Random().nextInt(pawns.size() - 1));
                        } else {
                            selected = pawns.get(0);
                        }

                        String from = this.getCurrentState().getBox(selected[0], selected[1]);

                        selected = empty.get(new Random().nextInt(empty.size() - 1));
                        String to = this.getCurrentState().getBox(selected[0], selected[1]);

                        try {
                            a = new Action(from, to, Turn.WHITE);
                        } catch (IOException e1) {
                            // TODO Auto-generated catch block
                            e1.printStackTrace();
                        }

                        try {
                            rules.checkMove(state, a);
                            found = true;
                        } catch (Exception e) {

                        }

                    }

                    System.out.println("Mossa scelta: " + a.toString());
                    pawns.clear();
                    empty.clear();
                    return a;

                }
                // Turno dell'avversario
                else if (state.getTurn().equals(Turn.BLACK)) {
                    System.out.println("Waiting for your opponent move... ");
                }
                // ho vinto
                else if (state.getTurn().equals(Turn.WHITEWIN)) {
                    System.out.println("YOU WIN!");
                    System.exit(0);
                }
                // ho perso
                else if (state.getTurn().equals(Turn.BLACKWIN)) {
                    System.out.println("YOU LOSE!");
                    System.exit(0);
                }
                // pareggio
                else if (state.getTurn().equals(Turn.DRAW)) {
                    System.out.println("DRAW!");
                    System.exit(0);
                }

            } else {

                // Mio turno
                if (this.getCurrentState().getTurn().equals(Turn.BLACK)) {
                    int[] buf;
                    for (int i = 0; i < state.getBoard().length; i++) {
                        for (int j = 0; j < state.getBoard().length; j++) {
                            if (state.getPawn(i, j).equalsPawn(State.Pawn.BLACK.toString())) {
                                buf = new int[2];
                                buf[0] = i;
                                buf[1] = j;
                                pawns.add(buf);
                            } else if (state.getPawn(i, j).equalsPawn(State.Pawn.EMPTY.toString())) {
                                buf = new int[2];
                                buf[0] = i;
                                buf[1] = j;
                                empty.add(buf);
                            }
                        }
                    }

                    int[] selected = null;

                    boolean found = false;
                    Action a = null;
                    try {
                        a = new Action("z0", "z0", Turn.BLACK);
                    } catch (IOException e1) {
                        // TODO Auto-generated catch block
                        e1.printStackTrace();
                    }
                    ;
                    while (!found) {
                        selected = pawns.get(new Random().nextInt(pawns.size() - 1));
                        String from = this.getCurrentState().getBox(selected[0], selected[1]);

                        selected = empty.get(new Random().nextInt(empty.size() - 1));
                        String to = this.getCurrentState().getBox(selected[0], selected[1]);

                        try {
                            a = new Action(from, to, Turn.BLACK);
                        } catch (IOException e1) {
                            // TODO Auto-generated catch block
                            e1.printStackTrace();
                        }

                        System.out.println("try: " + a.toString());
                        try {
                            rules.checkMove(state, a);
                            found = true;
                        } catch (Exception e) {

                        }

                    }

                    System.out.println("Mossa scelta: " + a.toString());
                    pawns.clear();
                    empty.clear();
                    return a;

                }

                else if (state.getTurn().equals(Turn.WHITE)) {
                    System.out.println("Waiting for your opponent move... ");
                } else if (state.getTurn().equals(Turn.WHITEWIN)) {
                    System.out.println("YOU LOSE!");
                    System.exit(0);
                } else if (state.getTurn().equals(Turn.BLACKWIN)) {
                    System.out.println("YOU WIN!");
                    System.exit(0);
                } else if (state.getTurn().equals(Turn.DRAW)) {
                    System.out.println("DRAW!");
                    System.exit(0);
                }

            }
        }
    }

    // Minimax�㷨�ĵݹ�ʵ��
    private int minimax(State state, int depth, int alpha, int beta, boolean maximizingPlayer) {
        if (depth == 0 || gameOver(state)) {
            return evaluateState(state);  // ������ǰ״̬
        }

        List<Action> possibleMoves = generateLegalMoves(state, maximizingPlayer ? Turn.WHITE : Turn.BLACK);

        if (maximizingPlayer) {
            int maxEval = Integer.MIN_VALUE;
            for (Action move : possibleMoves) {
                State newState = applyMove(state, move);
                int eval = minimax(newState, depth - 1, alpha, beta, false);
                maxEval = Math.max(maxEval, eval);
                alpha = Math.max(alpha, eval);
                if (beta <= alpha) break; // Alpha-Beta��֦
            }
            return maxEval;
        } else {
            int minEval = Integer.MAX_VALUE;
            for (Action move : possibleMoves) {
                State newState = applyMove(state, move);
                int eval = minimax(newState, depth - 1, alpha, beta, true);
                minEval = Math.min(minEval, eval);
                beta = Math.min(beta, eval);
                if (beta <= alpha) break; // Alpha-Beta��֦
            }
            return minEval;
        }
    }

    // ������ǰ״̬�ĵ÷�
    private int evaluateState(State state) {
        int score = 0;

        // ��ȡ�׷�������λ�ò����㵽�����ѵ�ľ���
        Position kingPos = getKingPosition(state);
        if (kingPos != null) {
            score += calculateKingDistanceToEscape(kingPos);
        }

        // �����׷�ʿ����λ��
        score += evaluateSoldiers(state, Turn.WHITE);

        // �����ڷ����ӵķֲ�
        score -= evaluateSoldiers(state, Turn.BLACK);

        // ��Ϸ��������������ʤ������÷�Ϊ�����Сֵ
        if (state.getTurn().equals(Turn.WHITEWIN)) {
            score += 1000;  // �׷�ʤ��
        } else if (state.getTurn().equals(Turn.BLACKWIN)) {
            score -= 1000;  // �ڷ�ʤ��
        }

        return score;
    }

    // ��ȡ������λ��
    private Position getKingPosition(State state) {
        for (int i = 0; i < state.getBoard().length; i++) {
            for (int j = 0; j < state.getBoard().length; j++) {
                if (state.getPawn(i, j) == State.Pawn.KING) {
                    return new Position(i, j);
                }
            }
        }
        return null; // ���û���ҵ�����������null
    }

    // �������������λ�õľ���
    private int calculateKingDistanceToEscape(Position kingPos) {
        // ��������λ�������̵Ľ���֮һ��������(0, 0)Ϊ���ѵ�
        return Math.abs(kingPos.getX() - 0) + Math.abs(kingPos.getY() - 0);
    }

    // ����ָ����ɫ��ʿ��λ��
    private int evaluateSoldiers(State state, Turn player) {
        int score = 0;
        for (int i = 0; i < state.getBoard().length; i++) {
            for (int j = 0; j < state.getBoard().length; j++) {
                if (state.getPawn(i, j) == (player == Turn.WHITE ? State.Pawn.WHITE : State.Pawn.BLACK)) {
                    score += 1;  // ���Ե������ֲ���
                }
            }
        }
        return score;
    }

    // ���ɺϷ����ƶ�
    private List<Action> generateLegalMoves(State state, Turn player) {
        List<Action> legalMoves = new ArrayList<>();

        // ���ÿ�������ϵ�����
        for (int i = 0; i < state.getBoard().length; i++) {
            for (int j = 0; j < state.getBoard().length; j++) {
                // ��ȡ����
                State.Pawn pawn = state.getPawn(i, j);
                // �ж����ӵ���ɫ�Ƿ�����ҵ���ɫһ��
                if ((player == Turn.WHITE && (pawn == State.Pawn.WHITE || pawn == State.Pawn.KING)) ||
                        (player == Turn.BLACK && pawn == State.Pawn.BLACK)) {

                    // ���ɸ����ӵ����кϷ��ƶ�
                    generateMovesForPawn(state, i, j, legalMoves);
                }
            }
        }

        return legalMoves;
    }

    // �������ӵ�λ�����ɸ����ӵĺϷ��ƶ�
    private void generateMovesForPawn(State state, int row, int col, List<Action> legalMoves) {
        // ��ȡ���ӵ�ǰλ�õı�ʶ
        String from = state.getBox(row, col);

        // ����ĸ������ϡ��¡����ң�
        int[] directions = {-1, 1};  // -1 ��ʾ����/����1 ��ʾ����/����
        for (int dr : directions) {
            for (int dc : directions) {
                int newRow = row + dr;
                int newCol = col + dc;

                // �����λ���Ƿ�Ϸ�
                if (isInBounds(newRow, newCol) && state.getPawn(newRow, newCol) == State.Pawn.EMPTY) {
                    // �����λ��Ϊ�գ�����ƶ��ǺϷ���
                    String to = state.getBox(newRow, newCol);
                    try {
                        legalMoves.add(new Action(from, to, state.getTurn()));
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
        }
    }

    // �����λ���Ƿ������̵ķ�Χ��
    private boolean isInBounds(int row, int col) {
        return row >= 0 && row < 9 && col >= 0 && col < 9; // ������һ�� 9x9 ������
    }

    // ִ�ж����������µ�״̬
    private State applyMove(State state, Action move) {
        // ��¡��ǰ״̬�������޸�ԭ״̬
        State newState = state.clone();

        // ��ȡ��������ʼλ�ú�Ŀ��λ��
        String from = move.getFrom();  // ��ʼλ��
        String to = move.getTo();      // Ŀ��λ��

        // ����ʼλ�õ������Ƴ�
        int fromRow = from.charAt(1) - '1'; // ��������Ϊ���� "a1" ��ʽ
        int fromCol = from.charAt(0) - 'a'; // ��������Ϊ���� "a1" ��ʽ
        newState.removePawn(fromRow, fromCol); // �Ƴ�ԭλ�õ�����

        // ��Ŀ��λ�õ����ӷ���
        int toRow = to.charAt(1) - '1';
        int toCol = to.charAt(0) - 'a';
        newState.getBoard()[toRow][toCol] = (newState.getTurn() == Turn.WHITE) ? State.Pawn.WHITE : State.Pawn.BLACK;

        // ������Ϸ�ĵ�ǰ�غ�
        newState.setTurn(newState.getTurn() == Turn.WHITE ? Turn.BLACK : Turn.WHITE);

        // ����������Լ������Ĺ������ж��Ƿ���Ϸ���������磺
        // 1. ����Ƿ������Ӯ����Ϸ
        // 2. ����Ƿ�ƽ��
        // 3. ����������������ȵ�

        return newState;  // ���ظ��º��״̬
    }

    // �ж���Ϸ�Ƿ����
    private boolean gameOver(State state) {
        return state.getTurn().equals(Turn.WHITEWIN) || state.getTurn().equals(Turn.BLACKWIN) || state.getTurn().equals(Turn.DRAW);
    }

    @Override
    public void run() {
        try {
            this.declareName();  // �����Լ�
        } catch (Exception e) {
            e.printStackTrace();
        }

        State state = null;
        Game rules = null;

        // ��Ϸ��������
        this.game = 4;  // ������Ϸ����
        switch (this.game) {
            case 1:
                state = new StateTablut();
                rules = new GameTablut();
                break;
            case 2:
                state = new StateTablut();
                rules = new GameModernTablut();
                break;
            case 3:
                state = new StateBrandub();
                rules = new GameTablut();
                break;
            case 4:
                state = new StateTablut();
                state.setTurn(State.Turn.WHITE);
                rules = new GameAshtonTablut(99, 0, "garbage", "fake", "fake");
                System.out.println("Ashton Tablut game");
                break;
            default:
                System.out.println("Error in game selection");
                System.exit(4);
        }

        System.out.println("You are player " + this.getPlayer().toString() + "!");

        while (true) {
            try {
                this.read();  // ��ȡ��ǰ״̬
                System.out.println("read end");

                // �ж��Ƿ����Լ��Ļغ�
                if (this.getPlayer().equals(this.getCurrentState().getTurn())) {
                    // ʹ��Minimax�㷨ѡ����Ѷ���
                    Action bestAction = bestMove(state, rules, this.getCurrentState());
                    if (bestAction == null) {
                        System.out.println("best action is null");
                        // ���û������߷������ѡ��һ���߷�
//                        bestAction = generateLegalMoves(this.getCurrentState(), this.getPlayer()).get(new Random().nextInt(legalMoves.size()));
                    }
                    this.write(bestAction); // ������Ѷ�����������
                    System.out.println("Sent best move: " + bestAction);
                } else {
                    System.out.println("Waiting for opponent's move...");
                }

            } catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
                break;
            }

            // �ȴ�һ����ʱ����ٽ�����һ���ж�
            try {
                Thread.sleep(1000);  // ��ͣ1�룬�������Ƶ����ѭ��
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
