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
    private static final int MAX_DEPTH = 3;  // Minimax搜索深度

    public TablutSmartClient(String player, String name, int game, int timeout, String ipAddress) throws IOException {
        super(player, name, timeout, ipAddress);
        this.game = game;
    }
    public TablutSmartClient(String player, String name) throws IOException {
        super(player, name);
    }

    // 计算最佳动作
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

        // 对每个合法动作进行评估
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

    // Minimax算法的递归实现
    private int minimax(State state, int depth, int alpha, int beta, boolean maximizingPlayer) {
        if (depth == 0 || gameOver(state)) {
            return evaluateState(state);  // 评估当前状态
        }

        List<Action> possibleMoves = generateLegalMoves(state, maximizingPlayer ? Turn.WHITE : Turn.BLACK);

        if (maximizingPlayer) {
            int maxEval = Integer.MIN_VALUE;
            for (Action move : possibleMoves) {
                State newState = applyMove(state, move);
                int eval = minimax(newState, depth - 1, alpha, beta, false);
                maxEval = Math.max(maxEval, eval);
                alpha = Math.max(alpha, eval);
                if (beta <= alpha) break; // Alpha-Beta剪枝
            }
            return maxEval;
        } else {
            int minEval = Integer.MAX_VALUE;
            for (Action move : possibleMoves) {
                State newState = applyMove(state, move);
                int eval = minimax(newState, depth - 1, alpha, beta, true);
                minEval = Math.min(minEval, eval);
                beta = Math.min(beta, eval);
                if (beta <= alpha) break; // Alpha-Beta剪枝
            }
            return minEval;
        }
    }

    // 评估当前状态的得分
    private int evaluateState(State state) {
        int score = 0;

        // 获取白方国王的位置并计算到达逃脱点的距离
        Position kingPos = getKingPosition(state);
        if (kingPos != null) {
            score += calculateKingDistanceToEscape(kingPos);
        }

        // 评估白方士兵的位置
        score += evaluateSoldiers(state, Turn.WHITE);

        // 评估黑方棋子的分布
        score -= evaluateSoldiers(state, Turn.BLACK);

        // 游戏结束情况：如果有胜负，则得分为极大或极小值
        if (state.getTurn().equals(Turn.WHITEWIN)) {
            score += 1000;  // 白方胜利
        } else if (state.getTurn().equals(Turn.BLACKWIN)) {
            score -= 1000;  // 黑方胜利
        }

        return score;
    }

    // 获取国王的位置
    private Position getKingPosition(State state) {
        for (int i = 0; i < state.getBoard().length; i++) {
            for (int j = 0; j < state.getBoard().length; j++) {
                if (state.getPawn(i, j) == State.Pawn.KING) {
                    return new Position(i, j);
                }
            }
        }
        return null; // 如果没有找到国王，返回null
    }

    // 计算国王到逃脱位置的距离
    private int calculateKingDistanceToEscape(Position kingPos) {
        // 假设逃脱位置是棋盘的角落之一，这里以(0, 0)为逃脱点
        return Math.abs(kingPos.getX() - 0) + Math.abs(kingPos.getY() - 0);
    }

    // 评估指定颜色的士兵位置
    private int evaluateSoldiers(State state, Turn player) {
        int score = 0;
        for (int i = 0; i < state.getBoard().length; i++) {
            for (int j = 0; j < state.getBoard().length; j++) {
                if (state.getPawn(i, j) == (player == Turn.WHITE ? State.Pawn.WHITE : State.Pawn.BLACK)) {
                    score += 1;  // 可以调整评分策略
                }
            }
        }
        return score;
    }

    // 生成合法的移动
    private List<Action> generateLegalMoves(State state, Turn player) {
        List<Action> legalMoves = new ArrayList<>();

        // 检查每个格子上的棋子
        for (int i = 0; i < state.getBoard().length; i++) {
            for (int j = 0; j < state.getBoard().length; j++) {
                // 获取棋子
                State.Pawn pawn = state.getPawn(i, j);
                // 判断棋子的颜色是否与玩家的颜色一致
                if ((player == Turn.WHITE && (pawn == State.Pawn.WHITE || pawn == State.Pawn.KING)) ||
                        (player == Turn.BLACK && pawn == State.Pawn.BLACK)) {

                    // 生成该棋子的所有合法移动
                    generateMovesForPawn(state, i, j, legalMoves);
                }
            }
        }

        return legalMoves;
    }

    // 根据棋子的位置生成该棋子的合法移动
    private void generateMovesForPawn(State state, int row, int col, List<Action> legalMoves) {
        // 获取棋子当前位置的标识
        String from = state.getBox(row, col);

        // 检查四个方向（上、下、左、右）
        int[] directions = {-1, 1};  // -1 表示向上/向左，1 表示向下/向右
        for (int dr : directions) {
            for (int dc : directions) {
                int newRow = row + dr;
                int newCol = col + dc;

                // 检查新位置是否合法
                if (isInBounds(newRow, newCol) && state.getPawn(newRow, newCol) == State.Pawn.EMPTY) {
                    // 如果新位置为空，则该移动是合法的
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

    // 检查新位置是否在棋盘的范围内
    private boolean isInBounds(int row, int col) {
        return row >= 0 && row < 9 && col >= 0 && col < 9; // 假设是一个 9x9 的棋盘
    }

    // 执行动作并返回新的状态
    private State applyMove(State state, Action move) {
        // 克隆当前状态，避免修改原状态
        State newState = state.clone();

        // 获取动作的起始位置和目标位置
        String from = move.getFrom();  // 起始位置
        String to = move.getTo();      // 目标位置

        // 将起始位置的棋子移除
        int fromRow = from.charAt(1) - '1'; // 假设输入为类似 "a1" 格式
        int fromCol = from.charAt(0) - 'a'; // 假设输入为类似 "a1" 格式
        newState.removePawn(fromRow, fromCol); // 移除原位置的棋子

        // 将目标位置的棋子放置
        int toRow = to.charAt(1) - '1';
        int toCol = to.charAt(0) - 'a';
        newState.getBoard()[toRow][toCol] = (newState.getTurn() == Turn.WHITE) ? State.Pawn.WHITE : State.Pawn.BLACK;

        // 更新游戏的当前回合
        newState.setTurn(newState.getTurn() == Turn.WHITE ? Turn.BLACK : Turn.WHITE);

        // 在这里你可以加入更多的规则来判断是否游戏结束，例如：
        // 1. 检查是否有玩家赢得游戏
        // 2. 检查是否平局
        // 3. 处理国王的特殊规则等等

        return newState;  // 返回更新后的状态
    }

    // 判断游戏是否结束
    private boolean gameOver(State state) {
        return state.getTurn().equals(Turn.WHITEWIN) || state.getTurn().equals(Turn.BLACKWIN) || state.getTurn().equals(Turn.DRAW);
    }

    @Override
    public void run() {
        try {
            this.declareName();  // 声明自己
        } catch (Exception e) {
            e.printStackTrace();
        }

        State state = null;
        Game rules = null;

        // 游戏规则设置
        this.game = 4;  // 设置游戏类型
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
                this.read();  // 获取当前状态
                System.out.println("read end");

                // 判断是否是自己的回合
                if (this.getPlayer().equals(this.getCurrentState().getTurn())) {
                    // 使用Minimax算法选择最佳动作
                    Action bestAction = bestMove(state, rules, this.getCurrentState());
                    if (bestAction == null) {
                        System.out.println("best action is null");
                        // 如果没有最佳走法，随机选择一个走法
//                        bestAction = generateLegalMoves(this.getCurrentState(), this.getPlayer()).get(new Random().nextInt(legalMoves.size()));
                    }
                    this.write(bestAction); // 发送最佳动作给服务器
                    System.out.println("Sent best move: " + bestAction);
                } else {
                    System.out.println("Waiting for opponent's move...");
                }

            } catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
                break;
            }

            // 等待一定的时间后再进行下一轮判断
            try {
                Thread.sleep(1000);  // 暂停1秒，避免过于频繁地循环
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
