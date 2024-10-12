import java.util.*;
import java.io.*;

public class Main {
    static BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
    static StringBuilder sb = new StringBuilder();
    static StringTokenizer st;

    static int turnCount;
    static int wallCount;

    static int[][] board;
    static Queue<Integer> walls;

    static boolean isEnd;

    static final int ROTATE_90 = 1;
    static final int ROTATE_180 = 2;
    static final int ROTATE_270 = 3;

    static final int[] DELTA_ROW = {-1,0,1,0};
    static final int[] DELTA_COL = {0,1,0,-1};

    static int result;

    public static void main(String[] args) throws Exception{

        st = new StringTokenizer(br.readLine().trim(), " ");
        turnCount = Integer.parseInt(st.nextToken());
        wallCount = Integer.parseInt(st.nextToken());

        board = new int[6][6];
        walls = new LinkedList<>();

        for(int row=1; row<=5; row++){
            st = new StringTokenizer(br.readLine().trim(), " ");
            for(int col=1; col<=5; col++){
                board[row][col] = Integer.parseInt(st.nextToken());
            }
        }

        st = new StringTokenizer(br.readLine().trim(), " ");

        for(int index=1; index<=wallCount; index++){
            walls.add(Integer.parseInt(st.nextToken()));
        }

        isEnd = false;

        for(int turn = 1; turn <= turnCount; turn++){
            if(isEnd)break;
            result = 0;
            solve();
            if(result != 0) sb.append(result + " ");
        }

        System.out.println(sb);
    }

    /**
     * 탐사 1회 수행

     270 * ( 탐사 1회 수행 시간)
     1. 회전으로 유물 획득의 최대 가능성을 확인한다.
     2. 해당 데이터를 기반으로 유물을 획득하고 연쇄 획득을 수행
     3. (1~2) 탐사 반복
     */
    static void solve(){
        RotateInfo rotateInfo = new RotateInfo();

        // 회전 시도
        for(int row=2; row<=4; row++){
            for(int col=2; col<=4; col++){
                for(int rotate=1; rotate<=3; rotate++){
                    int tempTreasureCount=0;
                    tempTreasureCount = processRotate(row, col, rotate);

                    if(rotateInfo.treasureCount < tempTreasureCount){
                        rotateInfo.row = row;
                        rotateInfo.col = col;
                        rotateInfo.rotate = rotate;
                        rotateInfo.treasureCount = tempTreasureCount;
                    }else if(rotateInfo.treasureCount == tempTreasureCount){
                        if(rotate < rotateInfo.rotate){
                            rotateInfo.row = row;
                            rotateInfo.col = col;
                            rotateInfo.rotate = rotate;
                            rotateInfo.treasureCount = tempTreasureCount;
                        }else if(rotate == rotateInfo.rotate){
                            if(col < rotateInfo.col){
                                rotateInfo.row = row;
                                rotateInfo.col = col;
                                rotateInfo.rotate = rotate;
                                rotateInfo.treasureCount = tempTreasureCount;
                            }else if(col == rotateInfo.col){
                                if(row < rotateInfo.row){
                                    rotateInfo.row = row;
                                    rotateInfo.col = col;
                                    rotateInfo.rotate = rotate;
                                    rotateInfo.treasureCount = tempTreasureCount;
                                }
                            }
                        }
                    }
                }
            }
        }

        // 회전으로 얻을 수 있는 유물이 없는 경우 종료
        if(rotateInfo.row==0 && rotateInfo.col == 0 && rotateInfo.rotate == 0){
            isEnd = true;
            return;
        }

        // 회전 처리
        tempBoard = new int[6][6];

        for(int row=1; row<=5; row++){
            tempBoard[row] = board[row].clone();
        }

        for(int curRow = rotateInfo.row-1; curRow <= rotateInfo.row+1; curRow++){
            for(int curCol = rotateInfo.col-1; curCol <= rotateInfo.col+1; curCol++){
                if(rotateInfo.rotate == ROTATE_90){
                    tempBoard[curCol][2 * rotateInfo.row - curRow] = board[curRow][curCol];
                }else if(rotateInfo.rotate == ROTATE_180){
                    tempBoard[curRow][curCol] = board[2 * rotateInfo.row - curRow][2*rotateInfo.col -curCol];
                }else if(rotateInfo.rotate == ROTATE_270){
                    tempBoard[curRow][curCol] = board[2*rotateInfo.col - curCol][curRow];
                }
            }
        }

        // 유물 획득
        result = adventureTreasure();

        for(int row=1; row<=5; row++){
            board[row] = tempBoard[row].clone();
        }
    }

    static int[][] tempBoard;
    static boolean[][] tempVisited;

    // row와 col 주변 3*3 사이즈의 데이터를 rotate 시킨다.
    static int processRotate(int centerRow, int centerCol, int rotate){
        int count =0;
        tempBoard = new int[6][6];
        tempVisited = new boolean[6][6];

        for(int row=1; row<=5; row++){
            tempBoard[row] = board[row].clone();
        }

        for(int curRow = centerRow-1; curRow <= centerRow+1; curRow++){
            for(int curCol = centerCol-1; curCol <= centerCol+1; curCol++){
                if(rotate == ROTATE_90){
                    tempBoard[curCol][2 * centerRow - curRow] = board[curRow][curCol];
                }else if(rotate == ROTATE_180){
                    tempBoard[curRow][curCol] = board[2 * centerRow - curRow][2 * centerCol - curCol];
                }else if(rotate == ROTATE_270){
                    tempBoard[curRow][curCol] = board[2 * centerCol -curCol][curRow];
                }
            }
        }

        //bfs로 유물의 갯수를 반환한다.
        for(int curRow = 1; curRow <= 5; curRow++){
            for(int curCol = 1; curCol <= 5; curCol++){
                if(tempVisited[curRow][curCol]) continue;
                count += countTreasure(new Point(curRow, curCol));
            }
        }

        return count;
    }

    // bfs로 사이즈가 3개 이상인 묶음들을 결과에 추가
    static int countTreasure(Point initPoint){
        int count=0;
        Queue<Point> queue = new LinkedList<>();

        queue.add(initPoint);
        tempVisited[initPoint.row][initPoint.col] = true;
        count++;

        while(!queue.isEmpty()){
            Point cur = queue.poll();

            for(int delta=0; delta<4; delta++){
                int dRow = cur.row + DELTA_ROW[delta];
                int dCol = cur.col + DELTA_COL[delta];

                if(dRow < 1 || dCol < 1 || dRow > 5 || dCol > 5) continue;
                if(tempVisited[dRow][dCol]) continue;
                if(tempBoard[dRow][dCol] != tempBoard[cur.row][cur.col]) continue;

                queue.add(new Point(dRow, dCol));
                tempVisited[dRow][dCol] = true;
                count++;
            }
        }

        if(count < 3) return 0;
        else return count;
    }

    // 유물을 획득하고 벽면의 숫자로 채운 뒤, 유물 연쇄 획득 가능성을 확인한다.
    // wall에서는 poll로 가져온다. Queue 이므로
    static int adventureTreasure(){
        int count =0;

        while(true){
            AdventureResult adventureResult = getTreasure();
            count += adventureResult.treasureCount;
            if(!adventureResult.recurYn) break;
        }

        return count;
    }

    // 유물 획득 후, 연쇄 가능성에 대한 결과를 반환한다.
    static AdventureResult getTreasure() {
        AdventureResult adventureResult = new AdventureResult(0, false);
        tempVisited = new boolean[6][6];

        //bfs로 유물의 갯수를 반환한다.
        totalLogQueue = new PriorityQueue<>((o1, o2)->{
            if(Integer.compare(o1.col, o2.col) == 0){
                return Integer.compare(o2.row, o1.row);
            }else{
                return Integer.compare(o1.col, o2.col);
            }
        });

        for(int curRow = 1; curRow <= 5; curRow++){
            for(int curCol = 1; curCol <= 5; curCol++){
                if(tempVisited[curRow][curCol]) continue;
                adventureResult.treasureCount += processTreasure(new Point(curRow, curCol));
            }
        }

        //wall에서 복구
        while(!totalLogQueue.isEmpty()){
            Point cur = totalLogQueue.poll();

            tempBoard[cur.row][cur.col] = walls.poll();
        }

        // 연쇄 가능 여부 확인
        tempVisited = new boolean[6][6];
        boolean flag = false;

        for(int curRow = 1; curRow <= 5; curRow++){
            if(flag) break;
            for(int curCol = 1; curCol <= 5; curCol++){
                if(tempVisited[curRow][curCol]) continue;
                if(countTreasure(new Point(curRow, curCol)) >= 3){
                    adventureResult.recurYn = true;
                    flag = true;
                    break;
                }
            }
        }

        return adventureResult;
    }

    static PriorityQueue<Point> totalLogQueue; // 사라지는 유물 이력 저장용

    static int processTreasure(Point initPoint){
        int count=0;
        Queue<Point> queue = new LinkedList<>();
        Queue<Point> logQueue = new LinkedList<>();

        queue.add(initPoint);
        logQueue.add(initPoint);
        tempVisited[initPoint.row][initPoint.col] = true;
        count++;

        while(!queue.isEmpty()){
            Point cur = queue.poll();

            for(int delta=0; delta<4; delta++){
                int dRow = cur.row + DELTA_ROW[delta];
                int dCol = cur.col + DELTA_COL[delta];

                if(dRow < 1 || dCol < 1 || dRow > 5 || dCol > 5) continue;
                if(tempVisited[dRow][dCol]) continue;
                if(tempBoard[dRow][dCol] != tempBoard[cur.row][cur.col]) continue;

                queue.add(new Point(dRow, dCol));
                logQueue.add(new Point(dRow, dCol));
                tempVisited[dRow][dCol] = true;
                count++;
            }
        }

        if(count < 3) return 0;
        else {
            // 3보다 큰 경우만 유물로 처리하고 벽의 값으로 교체해야함
            while(!logQueue.isEmpty()){
                totalLogQueue.add(logQueue.poll());
            }
            return count;
        }
    }

    static class RotateInfo {
        int row;
        int col;
        int rotate;
        int treasureCount;

        public RotateInfo(){

        }

        public RotateInfo(int row, int col, int rotate, int treasureCount){
            this.row = row;
            this.col = col;
            this.rotate = rotate;
            this.treasureCount = treasureCount;
        }
    }

    static class Point{
        int row;
        int col;

        public Point(int row, int col){
            this.row = row;
            this.col = col;
        }
    }

    static class AdventureResult{
        int treasureCount;
        boolean recurYn;

        public AdventureResult(int treasureCount, boolean recurYn){
            this.treasureCount = treasureCount;
            this.recurYn = recurYn;
        }
    }
}