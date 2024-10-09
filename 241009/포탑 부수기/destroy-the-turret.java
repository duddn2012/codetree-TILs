import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.StringTokenizer;

/**
 * 포탑 부수기
 *
 * 부서지지않은 포탑이 1개인 경우 종료
 */
public class Main {

    static StringTokenizer st;
    static BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
    static StringBuilder sb = new StringBuilder();

    static int rowSize;
    static int colSize;
    static int turnCount;
    static int liveTowerCount;

    static boolean isLiveTowerOnly;

    static Cell[][] board;
    static Point[][] parent;

    static PriorityQueue<Cell> attackPq = new PriorityQueue<>((o1, o2) -> {
        if(o1.isBroken && !o2.isBroken) return 1;
        else if (!o1.isBroken && o2.isBroken) return -1;

        if (o1.atk < o2.atk) {
            return -1;
        } else if (o1.atk == o2.atk) {
            if (o1.recentAtk > o2.recentAtk) {
                return -1;
            } else if (o1.recentAtk == o2.recentAtk) {
                if (o1.row + o1.col > o2.row + o2.col) {
                    return -1;
                } else if (o1.row + o1.col == o2.row + o2.col) {
                    // 열값이 가장 큰 포탑
                    return Integer.compare(o2.col, o1.col);
                } else {
                    return 1;
                }
            } else {
                return 1;
            }
        } else {
            return 1;
        }
    });

    static PriorityQueue<Cell> defencePq = new PriorityQueue<>((o1, o2) -> {
        if(o1.isBroken && !o2.isBroken) return 1;
        else if (!o1.isBroken && o2.isBroken) return -1;

        if(o1.atk > o2.atk){
            return -1;
        }else if(o1.atk == o2.atk){
            if(o1.recentAtk < o2.recentAtk){
                return -1;
            }else if(o1.recentAtk == o2.recentAtk){
                if(o1.row + o1.col < o2.row + o2.col){
                    return -1;
                }else if(o1.row + o1.col == o2.row+o2.col){
                    // 열값이 가장 작은 포탑
                    return Integer.compare(o1.col, o2.col);
                }else{
                    return 1;
                }
            }else {
                return 1;
            }
        }else{
            return 1;
        }
    });


    static final int[] DELTA_ROW = {0,1,0,-1, -1, 1, 1, -1};
    static final int[] DELTA_COL = {1,0,-1,0, 1, 1, -1, -1};

    static int[][] visited;
    static boolean[][] associatedAttack;

    static int result;

    public static void main(String[] args) throws Exception {
        st = new StringTokenizer(br.readLine().trim(), " ");
        rowSize = Integer.parseInt(st.nextToken());
        colSize = Integer.parseInt(st.nextToken());
        turnCount = Integer.parseInt(st.nextToken());

        board = new Cell[rowSize+1][colSize+1];
        liveTowerCount = rowSize * colSize;
        isLiveTowerOnly = false;

        for (int row=1; row<=rowSize; row++){
            st = new StringTokenizer(br.readLine().trim(), " ");
            for (int col=1; col<=colSize; col++){
                int atk = Integer.parseInt(st.nextToken());
                Cell cell = new Cell(atk, 0, row, col, false);

                if(atk <= 0){
                    cell.isBroken = true;
                    liveTowerCount--;
                    if(liveTowerCount == 1) isLiveTowerOnly = true;
                }

                board[row][col] = cell;
                attackPq.add(cell);
                defencePq.add(cell);
            }
        }

        for(int turn=1; turn<=turnCount; turn++){
            if(isLiveTowerOnly) break;
            
            visited = new int[rowSize+1][colSize+1];
            associatedAttack = new boolean[rowSize+1][colSize+1];
            parent = new Point[rowSize+1][colSize+1];
            solve(turn);
        }

        result =defencePq.peek().atk;

        sb.append(result);

        System.out.println(sb);
    }

    /**
     * 공격력이 0이하가 되면 부서짐
     * 부서지지 않은 포탑이 1개가 되면 즉시 중지
     *
     * 1. 공격자 선정
     * 공격자는 rowSize + colSize의 공격력을 얻는다.
     *  - 1-1. 공격력이 가장 낮은 포탑
     *  - 1-2. 가장 최근에 공격한 포탑
     *  -
     * 2. 공격자의 공격
     *  - 2-1. 가장 강한 포탑 선정
     *  - 2-2. 레이저 공격 OR 포탄 공격
     * 3. 포탑 부서짐
     * 4. 포탑 정비
     */
    static void solve(int turn){
        Cell attacker = selectAttacker();
        attacker.atk += rowSize + colSize;
        attacker.recentAtk = turn;

        Cell defencer = selectDefencer();

        controlPq(attacker);

        // Process Attack
        if(!attackRaser(attacker, defencer)){
            attackBomb(attacker, defencer);
        }

        if(isLiveTowerOnly) return;

        // 포탑 정비
        restoreTower();
    }

    static void restoreTower(){
        for(int row =1; row<=rowSize;row++){
            for(int col =1; col<=colSize;col++){
                Cell cur = board[row][col];

                if(cur.isBroken) continue;
                if(associatedAttack[row][col]) continue;

                cur.atk += 1;
                controlPq(cur);
            }
        }
    }


    static boolean attackRaser(Cell attacker, Cell defencer) {
        Queue<Point> queue = new LinkedList<>();
        queue.add(new Point(attacker.row, attacker.col, 1));
        visited[attacker.row][attacker.col] = 1;
        boolean isEnd = false;

        while(!queue.isEmpty()){
            Point cur = queue.poll();

            if(isEnd) break;

            for(int delta=0; delta<4; delta++){
                int dRow = cur.row + DELTA_ROW[delta];
                int dCol = cur.col + DELTA_COL[delta];

                if(dRow < 1) dRow = rowSize;
                if(dRow > rowSize) dRow = 1;
                if(dCol < 1) dCol = colSize;
                if(dCol > colSize) dCol = 1;

                if(visited[dRow][dCol] > 0) continue;
                if(board[dRow][dCol].isBroken) continue;

                visited[dRow][dCol] = cur.depth+1;
                parent[dRow][dCol] = cur;

                if(defencer.row == dRow && defencer.col == dCol){
                    isEnd = true;
                    break;
                }

                queue.add(new Point(dRow, dCol, cur.depth+1));
            }
        }

        // 최단 경로가 있는 경우만 찾는다.
        if(isEnd){
            findHistory(attacker, defencer);
            return true;
        }else return false;//공격에 실패한 경우 false

    }

    static void findHistory(Cell attacker, Cell defencer) {
        Queue<Point> queue = new LinkedList<>();
        queue.add(parent[defencer.row][defencer.col]);

        associatedAttack[defencer.row][defencer.col]  = true;
        associatedAttack[attacker.row][attacker.col]  = true;

        Cell defenceCell = board[defencer.row][defencer.col];

        defenceCell.atk -= attacker.atk;
        if(defenceCell.atk <= 0) {
            defenceCell.isBroken = true;
            liveTowerCount--;
            if(liveTowerCount == 1) isLiveTowerOnly = true;
        }
        controlPq(defenceCell);

        while(!queue.isEmpty()){
            Point cur = queue.poll();
            if(cur.row == attacker.row && cur.col == attacker.col) break;
            if(isLiveTowerOnly) break;

            Cell dCell = board[cur.row][cur.col];
            dCell.atk -= attacker.atk /2;
            if(dCell.atk <= 0) {
                dCell.isBroken = true;
                liveTowerCount--;
                if(liveTowerCount == 1) isLiveTowerOnly = true;
            }

            controlPq(dCell);

            associatedAttack[cur.row][cur.col]  = true;
            queue.add(parent[cur.row][cur.col]);
        }
    }

    static void attackBomb(Cell attacker, Cell defencer){
        int row = defencer.row;
        int col = defencer.col;

        associatedAttack[row][col]  = true;
        associatedAttack[attacker.row][attacker.col]  = true;

        defencer.atk -= attacker.atk;
        if(defencer.atk <= 0) {
            defencer.isBroken = true;
            liveTowerCount--;
            if(liveTowerCount == 1) isLiveTowerOnly = true;
        }
        controlPq(defencer);

        for(int delta=0; delta<8; delta++){
            if(isLiveTowerOnly) return;

            int dRow = row + DELTA_ROW[delta];
            int dCol = col + DELTA_COL[delta];

            if(dRow < 1) dRow = rowSize;
            if(dRow > rowSize) dRow = 1;
            if(dCol < 1) dCol = colSize;
            if(dCol > colSize) dCol = 1;

            if(board[dRow][dCol].isBroken) continue;
            if(dRow == attacker.row && dCol == attacker.col) continue;

            Cell dCell = board[dRow][dCol];
            dCell.atk -= attacker.atk /2;
            if(dCell.atk <= 0) {
                dCell.isBroken = true;
                liveTowerCount--;
                if(liveTowerCount == 1) isLiveTowerOnly = true;
            }
            controlPq(dCell);

            associatedAttack[dRow][dCol]  = true;
        }
    }

    static Cell selectDefencer(){
        return defencePq.peek();
    }

    static Cell selectAttacker() {
        return attackPq.peek();
    }

    static void controlPq(Cell cell){
        attackPq.remove(cell);
        attackPq.add(cell);

        defencePq.remove(cell);
        defencePq.add(cell);
    }

    static class Cell implements Comparable{
        int atk;
        int recentAtk;
        int row;
        int col;
        boolean isBroken;

        public Cell(int atk, int recentAtk, int row, int col, boolean isBroken) {
            this.atk = atk;
            this.recentAtk = recentAtk;
            this.row = row;
            this.col = col;
            this.isBroken = isBroken;
        }

        @Override
        public int compareTo(Object o) {
            return 0;
        }
    }

    static class Point {
        int row;
        int col;
        int depth;

        public Point(int row, int col, int depth) {
            this.row = row;
            this.col = col;
            this.depth = depth;
        }
    }

}