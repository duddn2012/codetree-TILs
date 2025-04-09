
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.StringTokenizer;
import java.util.concurrent.atomic.AtomicInteger;

/**
 *
 * CodeTree 2024 하반기 오후 1번 문제 / 메두사와 전사들
 *
 * N * N 보드
 * 0 도로, 1 도로 X
 *
 * 메두사
 * 집에서 공원까지 가야함
 * 도로만을 따라 최단 경로로 공원까지 이동
 * 집과 공원은 항상 도로위(0)
 * 메두사는 전사를 바라볼 때 전사들은 움직일 수 없다
 *
 *
 * 전사
 * 다수의 전사 존재
 * 메두사를 향해 최단 경로로 이동
 * 전사들은 도로와 비도로를 구분하지 않고 어느 칸이든 이동 가능
 *
 * 턴
 * 메두사는 도로를 따라 한 칸 이동, 공원까지 최단 경로
 * 메두사가 이동한 칸에 전사가 있을 경우 전사는 사라짐
 * 최단경로는 상,하,좌,우 우선 순위를 따름
 * 메두사의 집에서 공원까지 도달하는 경로가 없을 수도 있음
 *
 * 메두사의 시선
 * 상,하,좌,우 현재 방향의 90도
 * 전사가 가려질 경우 그 범위만큼 보이지 않음
 * 바라 볼 경우 움직임 불가
 * 메두사는 상하좌우 중 전사를 가장 많이 볼 수 있는 방향을 바라봄, 상하좌우 우선순위
 *
 *
 * 전사의 움직임
 * 전사들은 최대 두 칸까지 이동 가능
 * 첫 이동: 메두사와의 거리를 줄일 수 있는 방향으로 한칸 이동, 상하좌우 우선순위
 * 격자의 바깥으로 나갈 수 없으며, 메두사의 시야에 들어오는 곳으로는 이동 불가
 * 두 이동:
 * 첫 이동: 메두사와의 거리를 줄일 수 있는 방향으로 한칸 이동, 상하좌우 우선순위
 * 격자의 바깥으로 나갈 수 없으며, 메두사의 시야에 들어오는 곳으로는 이동 불가
 *
 * 전사의 공격
 * 메두사와 같은 칸에 도달한 전사는 사망(사라짐)
 *
 * 최단 경로 기준은 맨해튼 거리를 기준으로 함.
 * 메두사가 공원에 도달할 때까지 매 턴마다 해당 턴에서
 * 1. 모든 턴에서 전사가 이동한 거리의 합,
 * 2. 메두사로 인해 돌이 된 전사의 수
 * 3. 메두사를 공격한 전사의 수
 * 공백을 사이에 두고 차례대로 출력
 *
 * 메두사가 공원에 도착하는 턴에는 0을 출력
 *
 *
 * 1. 메두사 공원으로의 최단 경로 확인
 * 2. 메두사의 시선 발동
 * 3. 전사 이동
 * 4. 전사 공격(사망)
 *
 */

public class Main {

    static BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
    static StringTokenizer st;
    static StringBuilder sb = new StringBuilder();

    static int boardSize;
    static int warriorCount;

    /**
     * 도로 0, 도로 X 1
     */
    static int[][] board;
    static int[][] lookBoard;

    static Point medusaLocation, parkLocation;
    static List<Point> warriorLocations = new ArrayList<>();
    static int[][] warriorCountArr;

    // 상 우상 우 우하 하 좌하 좌 좌상
    static final int[] ROW_DIRECTION = {-1, -1, 0, 1, 1, 1, 0, -1};
    static final int[] COL_DIRECTION = {0, 1, 1, 1, 0, -1, -1, -1};

    static final int[] STRAIGHT_DIRECTION_INDEX = {0, 4, 6, 2};

    static Point[][] visited;
    static Deque<Point> medusaPath = new ArrayDeque<>();
    static int[][] medusaLookLocations;

    public static void main(String args[]) throws IOException {

        try{
            init();

            solve();

            sb.append(0);

            System.out.println(sb);

        }catch (AlgorithmException e){
            System.out.println(-1);
        }
    }

    static void init() throws IOException, AlgorithmException {

        st = new StringTokenizer(br.readLine(), " ");

        boardSize = Integer.parseInt(st.nextToken());
        warriorCount = Integer.parseInt(st.nextToken());

        board = new int[boardSize + 1][boardSize + 1];
        visited = new Point[boardSize + 1][boardSize + 1];

        st = new StringTokenizer(br.readLine(), " ");

        medusaLocation = new Point(Integer.parseInt(st.nextToken()), Integer.parseInt(st.nextToken()));
        parkLocation = new Point(Integer.parseInt(st.nextToken()), Integer.parseInt(st.nextToken()));

        // 전사 위치 입력
        st = new StringTokenizer(br.readLine(), " ");
        for(int index=0; index < warriorCount; index++) {
            Point warrirorLocation = new Point(Integer.parseInt(st.nextToken()), Integer.parseInt(st.nextToken()));
            warriorLocations.add(warrirorLocation);
        }

        // 보드 입력
        for(int row = 0; row < boardSize; row++) {
            st = new StringTokenizer(br.readLine(), " ");

            for(int col = 0; col < boardSize; col++) {
                board[row][col] = Integer.parseInt(st.nextToken());
            }
        }

        // 메두사 최단 경로 세팅
        makeMedusaPath();
        restoreVisitedPath();
    }

    static void makeMedusaPath() throws AlgorithmException {
        Queue<Point> queue = new LinkedList<>();
        boolean flag = false;
        queue.add(medusaLocation);
        visited[medusaLocation.row][medusaLocation.col] = new Point(-1,-1);

        while(!queue.isEmpty()) {
            Point point = queue.remove();

            if(point.row == parkLocation.row && point.col == parkLocation.col) {
                flag = true;
                break;
            }

            for(int index : STRAIGHT_DIRECTION_INDEX) {
                int curRow = point.row + ROW_DIRECTION[index];
                int curCol = point.col + COL_DIRECTION[index];

                if(isEnableLocation(curRow, curCol)){
                    visited[curRow][curCol] = new Point(point.row, point.col);
                    queue.add(new Point(curRow, curCol));
                }
            }
        }

        if(!flag){
            throw new AlgorithmException();
        }

    }

    // visited에는 이전 이동 좌표
    // queue에는 현재 봐야할 좌표값 저장
    static void restoreVisitedPath() {
        Queue<Point> queue = new LinkedList<>();
        queue.add(visited[parkLocation.row][parkLocation.col]);


        while(!queue.isEmpty()) {
            Point point = queue.remove();

            if(point.row == medusaLocation.row && point.col == medusaLocation.col) break;

            medusaPath.push(new Point(point.row, point.col));
            queue.add(visited[point.row][point.col]);

        }

    }

    static boolean isWarriorMoveEnableLocation(int curRow, int curCol) {
        if(curRow < 0 || curCol < 0 || curRow >= boardSize || curCol >= boardSize) return false;
        if(lookBoard[curRow][curCol] == 1 || lookBoard[curRow][curCol] == 3) return false;
        return true;
    }

    static boolean isLookEnableLocation(int curRow, int curCol) {
        if(curRow < 0 || curCol < 0 || curRow >= boardSize || curCol >= boardSize) return false;
        if(lookBoard[curRow][curCol] > 0) return false;
        return true;
    }

    static boolean isEnableLocation(int curRow, int curCol) {
        if(curRow < 0 || curCol < 0 || curRow >= boardSize || curCol >= boardSize) return false;
        if(board[curRow][curCol] > 0) return false;
        if(visited[curRow][curCol] != null) return false;
        return true;
    }

    static void solve() {
        // 턴 진행
        while(!medusaPath.isEmpty()) {
            int warriorMoveCount = 0;
            int stoneCount = 0;
            int warriorAttackCount = 0;

            // 메두사 이동
            medusaLocation = medusaPath.pop();

            warriorAttack();

            // 메두사 시선
            int maxStone = -1;
            int maxDirectionIndex = 0;

            // 1. 4방향 탐색
            for(int index : STRAIGHT_DIRECTION_INDEX) {

                int tempStone = medusaLook(index, medusaLocation.row, medusaLocation.col);

                if(tempStone > maxStone) {
                    maxStone = tempStone;
                    maxDirectionIndex = index;
                }
            }

            // lookBoard 상태 업데이트를 위한 호출
            medusaLook(maxDirectionIndex, medusaLocation.row, medusaLocation.col);
            stoneCount = maxStone;

            // printBoard();

            // 전사들의 이동
            warriorMoveCount = warriorMove();

            // 전사의 공격
            warriorAttackCount += warriorAttack();

            sb.append(warriorMoveCount + " " + stoneCount + " " + warriorAttackCount + "\n");
        }
    }

    static int warriorAttack() {
        int[] attackCount = {0};
        warriorLocations.removeIf(point -> {
            if (point.row == medusaLocation.row && point.col == medusaLocation.col) {
                attackCount[0]++;
                return true;
            } else
                return false;
        });

        return attackCount[0];
    }

    static void printBoard() {
        System.out.println("-----------------");
        for(int row=0; row < boardSize; row++) {
            for(int col=0; col < boardSize; col++) {
                if (row == medusaLocation.row && col == medusaLocation.col) {
                    System.out.print("M ");
                }else System.out.print(lookBoard[row][col] + " ");
            }
            System.out.println();
        }
    }

    static int warriorMove() {

        int warriorMoveCount = 0;

        // 첫 번째 움직임 상하좌우
        for(Point point: warriorLocations) {
            int newRow, newCol,  oldDistance, newDistance;
            int medusaRow = medusaLocation.row;
            int medusaCol = medusaLocation.col;
            int oldRow = point.row;
            int oldCol = point.col;

            if(lookBoard[oldRow][oldCol] == 3) continue;

            for(int directionIndex: STRAIGHT_DIRECTION_INDEX) {
                newRow = point.row + ROW_DIRECTION[directionIndex];
                newCol = point.col + COL_DIRECTION[directionIndex];

                if(isWarriorMoveEnableLocation(newRow, newCol)){
                    oldDistance = Math.abs(medusaRow - oldRow) + Math.abs(medusaCol - oldCol);
                    newDistance = Math.abs(medusaRow - newRow) + Math.abs(medusaCol - newCol);
                }else {
                    continue;
                }


                if(newDistance < oldDistance) {
                    point.row = newRow;
                    point.col = newCol;
                    warriorMoveCount++;
                    break;
                }
            }

            // 두 번째 움직임 좌우상하
            int[] ANOTHER_STRAIGHT_DIRECTION_INDEX = new int[]{6,2,0,4};
            for(int directionIndex: ANOTHER_STRAIGHT_DIRECTION_INDEX) {
                newRow = point.row + ROW_DIRECTION[directionIndex];
                newCol = point.col + COL_DIRECTION[directionIndex];

                if(isWarriorMoveEnableLocation(newRow, newCol)){
                    oldDistance = Math.abs(medusaRow - oldRow) + Math.abs(medusaCol - oldCol);
                    newDistance = Math.abs(medusaRow - newRow) + Math.abs(medusaCol - newCol);
                }else {
                    continue;
                }


                if(newDistance < oldDistance) {
                    point.row = newRow;
                    point.col = newCol;
                    warriorMoveCount++;
                    break;
                }
            }
        }

        return warriorMoveCount;
    }

    static int medusaLook(int directionIndex, int row, int col) {

        int stoneCount = 0;
        lookBoard = new int[boardSize+1][boardSize+1];
        updatePeopleCountStatus();

        stoneCount += wideLook(directionIndex, (directionIndex-1 + 8) % 8, row, col);
        stoneCount += wideLook(directionIndex, (directionIndex) % 8, row, col);
        stoneCount += wideLook(directionIndex, (directionIndex+1 + 8) % 8, row, col);

        return stoneCount;
    }

    static void updatePeopleCountStatus() {
        warriorCountArr = new int[boardSize + 1][boardSize + 1];
        for(Point point: warriorLocations) {
            int row = point.row;
            int col = point.col;

            warriorCountArr[row][col]++;
        }
    }

    static int wideLook(int straightDirectionIndex, int directionIndex, int row, int col) {
        int curRow = row;
        int curCol = col;

        int stoneCount = 0;

        while(true) {
            curRow += ROW_DIRECTION[directionIndex];
            curCol += COL_DIRECTION[directionIndex];

            if(isLookEnableLocation(curRow, curCol)) {

                // 전사를 만났을 경우 stone 처리
                if(meetWarrior(curRow, curCol)) {
                    stoneCount += warriorCountArr[curRow][curCol];
                    lookBoard[curRow][curCol] = 3;
                    makeSafeArea(straightDirectionIndex, straightDirectionIndex, curRow, curCol);
                    makeSafeArea(straightDirectionIndex, directionIndex, curRow, curCol);
                }else {
                    lookBoard[curRow][curCol] = 1;  //방문처리
                }

                stoneCount += lookStraight(0, straightDirectionIndex, directionIndex, curRow, curCol);
            }
            else break;
        }

        return stoneCount;
    }

    static void makeSafeArea(int straightDirectionIndex, int currentDirectionIndex, int row, int col) {
        int curRow = row;
        int curCol = col;

        while(true) {
            curRow += ROW_DIRECTION[currentDirectionIndex];
            curCol += COL_DIRECTION[currentDirectionIndex];

            if(isLookEnableLocation(curRow, curCol)) {
                lookBoard[curRow][curCol] = 2;  // 그림자 방문 처리

                lookStraight(1, straightDirectionIndex, currentDirectionIndex, curRow, curCol);
            }
            else break;
        }
    }

    // type 0 = 메두사의 시선
    // type 1 = 전사의 그림자
    static int lookStraight(int type, int straightDirectionIndex, int directionIndex, int row, int col) {
        int curRow = row;
        int curCol = col;

        int straightStoneCount = 0;

        while(true) {
            curRow += ROW_DIRECTION[straightDirectionIndex];
            curCol += COL_DIRECTION[straightDirectionIndex];

            if(isLookEnableLocation(curRow, curCol)) {
                int setValue = type == 0? 1 : 2;
                lookBoard[curRow][curCol] = setValue;

                // 전사를 만났을 경우 stone 처리
                if(type == 0 && meetWarrior(curRow, curCol)) {
                    straightStoneCount += warriorCountArr[curRow][curCol];
                    lookBoard[curRow][curCol] = 3;
                    makeSafeArea(straightDirectionIndex, straightDirectionIndex, curRow, curCol);
                    makeSafeArea(straightDirectionIndex, directionIndex, curRow, curCol);
                }
            }
            else break;
        }

        return straightStoneCount;
    }

    static boolean meetWarrior(int curRow, int curCol) {
        for(Point point: warriorLocations) {
            int row = point.row;
            int col = point.col;

            if(row == curRow && col == curCol) {
                return true;
            }
        }

        return false;
    }

    static class Point {
        int row;
        int col;

        Point(int row, int col) {
            this.row = row;
            this.col = col;
        }
    }

    static class AlgorithmException extends RuntimeException {
    }

}
