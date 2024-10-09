import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.LinkedList;
import java.util.Queue;
import java.util.StringTokenizer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.PriorityQueue;
import java.util.StringTokenizer;

public class Main {

    private static final int[][] DELTAS_FOR_ATTACK1 = {{0, 1}, {1, 0}, {0, -1}, {-1, 0}};
    private static final int[][] DELTAS_FOR_ATTACK2 = {{0, 1}, {1, 1}, {1, 0}, {1, -1}, {0, -1}, {-1, -1}, {-1, 0}, {-1, 1}};
    private static final int X = 2;
    private static final int Y = 3;
    private static final int POWER = 0;
    private static final int LAST_ATTACK = 1;
    private static final int BROKEN = 0;
    private static final int DISTANCE = 2;
    private static final int DIRECTION = 3;
    private static final PriorityQueue<int[]> WEAKEST = new PriorityQueue<>((t1, t2) -> {
        if (t1[POWER] == t2[POWER]) {
            if (t1[LAST_ATTACK] == t2[LAST_ATTACK]) {
                if ((t1[X] + t1[Y]) == (t2[X] + t2[Y])) {
                    return t2[Y] - t1[Y];
                }
                return (t2[X] + t2[Y]) - (t1[X] + t1[Y]);
            }
            return t2[LAST_ATTACK] - t1[LAST_ATTACK];
        }
        return t1[POWER] - t2[POWER];
    });
    private static final PriorityQueue<int[]> STRONGEST = new PriorityQueue<>((t1, t2) -> {
        if (t1[POWER] == t2[POWER]) {
            if (t1[LAST_ATTACK] == t2[LAST_ATTACK]) {
                if (t1[X] + t1[Y] == t2[X] + t2[Y]) {
                    return t1[Y] - t2[Y];
                }
                return (t1[X] + t1[Y]) - (t2[X] + t2[Y]);
            }
            return t1[LAST_ATTACK] - t2[LAST_ATTACK];
        }
        return t2[POWER] - t1[POWER];
    });
    private static final PriorityQueue<int[]> MOVES = new PriorityQueue<>((m1, m2) -> {
        if (m1[DISTANCE] == m2[DISTANCE]) {
            return m1[DIRECTION] - m2[DIRECTION];
        }
        return m1[DISTANCE] - m2[DISTANCE];
    });

    private static int n;
    private static int m;
    private static int[][][] map;
    private static int[][] distances;
    private static boolean[][] isVisited;
    private static boolean[][] isRelated;
    private static int[][][] roots;

    public static void main(String[] args) throws IOException {
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(System.in));
        StringTokenizer stringTokenizer = new StringTokenizer(bufferedReader.readLine());
        n = Integer.parseInt(stringTokenizer.nextToken());
        m = Integer.parseInt(stringTokenizer.nextToken());
        int k = Integer.parseInt(stringTokenizer.nextToken());
        map = new int[n][m][2];
        distances = new int[n][m];
        isVisited = new boolean[n][m];
        isRelated = new boolean[n][m];
        roots = new int[n][m][2];
        for (int i = 0; i < n; i++) {
            stringTokenizer = new StringTokenizer(bufferedReader.readLine());
            for (int j = 0; j < m; j++) {
                map[i][j][POWER] = Integer.parseInt(stringTokenizer.nextToken());
            }
        }
        int time = 1;
        while (time <= k) {
            init();
            addTopInfos();
            int[] attacker = WEAKEST.poll();
            int[] target = STRONGEST.poll();
            if (attacker[X] == target[X] && attacker[Y] == target[Y]) {
                target = STRONGEST.poll();
            }
            if (target == null) {
                break;
            }
            attacker[POWER] += n + m;
            map[attacker[X]][attacker[Y]][POWER] = attacker[POWER];
            map[attacker[X]][attacker[Y]][LAST_ATTACK] = time;
            isRelated[attacker[X]][attacker[Y]] = true;
            isRelated[target[X]][target[Y]] = true;
            if (!findTargetByAttack1(attacker, target)) {
                attack2(attacker, target);
            }
            increasePower();
            time++;
        }
        System.out.println(getMaxPower());
    }

    private static void init() {
        WEAKEST.clear();
        STRONGEST.clear();
        MOVES.clear();
        for (int i = 0; i < n; i++) {
            Arrays.fill(isVisited[i], false);
            Arrays.fill(isRelated[i], false);
            Arrays.fill(distances[i], Integer.MAX_VALUE);
            for (int j = 0; j < m; j++) {
                Arrays.fill(roots[i][j], 0);
            }
        }
    }

    private static void addTopInfos() {
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < m; j++) {
                if (map[i][j][POWER] != BROKEN) {
                    WEAKEST.offer(new int[]{map[i][j][POWER], map[i][j][LAST_ATTACK], i, j});
                    STRONGEST.offer(new int[]{map[i][j][POWER], map[i][j][LAST_ATTACK], i, j});
                }
            }
        }
    }

    private static boolean findTargetByAttack1(int[] attacker, int[] target) {
        MOVES.offer(new int[]{attacker[X], attacker[Y], 0, 0});
        distances[attacker[X]][attacker[Y]] = 0;
        while (!MOVES.isEmpty()) {
            int[] current = MOVES.poll();
            int x = current[0];
            int y = current[1];
            int distance = current[DISTANCE];
            if (x == target[X] && y == target[Y]) {
                attack1(attacker, target);
                return true;
            }
            if (isVisited[x][y]) {
                continue;
            }
            isVisited[x][y] = true;
            for (int direction = 0; direction < 4; direction++) {
                int dx = (x + DELTAS_FOR_ATTACK1[direction][0] + n) % n;
                int dy = (y + DELTAS_FOR_ATTACK1[direction][1] + m) % m;
                if (map[dx][dy][POWER] != BROKEN && !isVisited[dx][dy] && distances[dx][dy] > distance + 1) {
                    roots[dx][dy][0] = x;
                    roots[dx][dy][1] = y;
                    distances[dx][dy] = distance + 1;
                    MOVES.offer(new int[]{dx, dy, distance + 1, current[DIRECTION] * 10 + direction});
                }
            }
        }
        return false;
    }

    private static void attack1(int[] attacker, int[] target) {
        int attack = attacker[POWER];
        map[target[X]][target[Y]][POWER] -= attack;
        checkBroken(target[X], target[Y]);
        attack /= 2;
        int x = roots[target[X]][target[Y]][0];
        int y = roots[target[X]][target[Y]][1];
        while (x != attacker[X] || y != attacker[Y]) {
            isRelated[x][y] = true;
            map[x][y][POWER] -= attack;
            checkBroken(x, y);
            int nextX = roots[x][y][0];
            int nextY = roots[x][y][1];
            x = nextX;
            y = nextY;
        }
    }

    private static void attack2(int[] attacker, int[] target) {
        int attack = attacker[POWER];
        map[target[X]][target[Y]][POWER] -= attack;
        checkBroken(target[X], target[Y]);
        attack /= 2;
        int x = target[X];
        int y = target[Y];
        for (int[] delta : DELTAS_FOR_ATTACK2) {
            int dx = (x + delta[0] + n) % n;
            int dy = (y + delta[1] + m) % m;
            if ((dx != attacker[X] || dy != attacker[Y]) && map[dx][dy][POWER] != BROKEN) {
                map[dx][dy][POWER] -= attack;
                checkBroken(dx, dy);
                isRelated[dx][dy] = true;
            }
        }
    }

    private static void checkBroken(int x, int y) {
        if (map[x][y][POWER] < 0) {
            map[x][y][POWER] = 0;
        }
    }

    private static void increasePower() {
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < m; j++) {
                if (map[i][j][POWER] != BROKEN && !isRelated[i][j]) {
                    map[i][j][POWER]++;
                }
            }
        }
    }

    private static int getMaxPower() {
        int max = 0;
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < m; j++) {
                max = Math.max(max, map[i][j][POWER]);
            }
        }
        return max;
    }
}