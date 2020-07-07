package com.intgroup.htmlcheck.feature.block;

import com.intgroup.htmlcheck.domain.security.User;
import com.intgroup.htmlcheck.feature.pref.GlobalPrefService;
import com.intgroup.htmlcheck.feature.shortlink.CuttlyService;
import com.intgroup.htmlcheck.feature.taskstat.UserTaskStatService;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.*;

@Service
public class CheckTaskBlockService {
    public static final String RESUME_LINK = "https://cutt.ly/ct92dkg";

    public static final String FRONTEND_URL_PREF = "taskBlockServiceFrontendUrlPref";

    private Map<Integer, List<String>> blockMap;
    private Map<String, Integer> blockIndexHashes;

    private Map<Integer, List<String>> singleTaskBlockMap;
    private Map<String, Integer> singleTaskBlockIndexHashes;

    private String frontendUrl;

    @Autowired
    private CuttlyService cuttlyService;

    @Autowired
    private GlobalPrefService globalPrefService;

    @Autowired
    private TaskBlockHashNamingService hashNamingService;

    @PostConstruct
    public void init() {
        blockMap = new LinkedHashMap<>();

        blockMap.put(0, Arrays.asList("html-1", "html-2", "html-3", "html-4", "html-5", "html-6", "html-7", "html-8", "html-9", "html-10",
                "html-11", "html-12", "html-13", "html-14", "html-15",
                "css-1", "css-2", "css-3", "css-4", "css-5", "css-6", "css-7", "css-8", "css-9", "css-10", "css-11", "css-12", "css-13", "css-14", "css-15", "css-16", "css-17",
                "css-18", "css-19", "css-20", "css-21", "css-22", "css-23", "css-24", "css-25", "css-26", "css-27", "css-28", "css-29"));
        blockMap.put(1, Arrays.asList("html-1", "html-2", "html-3", "html-4", "html-5", "html-6", "html-7", "html-8", "html-9", "html-10"));
        blockMap.put(2, Arrays.asList("html-11", "html-12", "html-13", "html-14", "html-15"));
        blockMap.put(3, Arrays.asList("css-1", "css-2", "css-3", "css-4", "css-5", "css-6", "css-7", "css-8", "css-9", "css-10", "css-11", "css-12", "css-13", "css-14", "css-15", "css-16", "css-17"));
        blockMap.put(4, Arrays.asList("css-18", "css-19", "css-20", "css-21", "css-22", "css-23", "css-24", "css-25", "css-26", "css-27", "css-28", "css-29"));
        blockMap.put(5, Arrays.asList("css-netlify"));
        blockMap.put(6, Arrays.asList("css-30", "css-31", "css-32", "css-33", "css-34", "css-35", "css-36"));

        //JS
        blockMap.put(7, Arrays.asList(
                "html-teens-1",
                "css-teens-2",
                "js-teens-3",
                "html-teens-4",
                "css-teens-5",
                "js-teens-6",
                "html-teens-7",
                "css-teens-8",
                "js-teens-9",
                "html-teens-10",
                "css-teens-11",
                "js-teens-12",
                "html-teens-13",
                "css-teens-14",
                "js-teens-15",
                "html-teens-16",
                "css-teens-17",
                "js-teens-18",
                "html-teens-19",
                "css-teens-20",
                "js-teens-21",
                "html-teens-22",
                "css-teens-23",
                "js-teens-24",
                "html-teens-25",
                "css-teens-26",
                "js-teens-27",
                "css-teens-28",
                "css-teens-29",
                "js-teens-30",
                "html-teens-31",
                "css-teens-32",
                "js-teens-33",
                "js-teens-34",
                "js-teens-35",
                "html-teens-36"));

        //Teen 33 - HTML&CSS&JS
        String[][] teen33Blocks =  {
                {  "html-teens-1", "css-teens-2", "js-teens-3", "html-teens-4", "css-teens-5", "js-teens-6", "html-teens-7", "css-teens-8", "js-teens-9", "html-teens-10", "css-teens-11", "js-teens-12"},
                { "html-teens-13", "css-teens-14", "js-teens-15", "html-teens-16", "css-teens-17", "js-teens-18", "html-teens-19", "css-teens-20", "js-teens-21", "html-teens-22", "css-teens-23", "js-teens-24"},
                { "html-teens-25", "css-teens-26", "js-teens-27", "css-teens-28", "css-teens-29", "js-teens-30", "html-teens-31", "css-teens-32", "js-teens-33", "js-teens-34", "js-teens-35", "html-teens-36"}
        };

        int startTeenBlockIndex = 8;
        for(int i = 0; i < 3; i++) {
            int teenBlockIndex = startTeenBlockIndex + i;
            blockMap.put(teenBlockIndex, Arrays.asList(teen33Blocks[i]));
        }

        //Teen 33 - Java
        blockMap.put(11, Arrays.asList(
                "java-teens-methods",
                "java-teens-method-declaration",
                "java-teens-enter-name",
                "java-teens-string-concat",
                "java-teens-division",
                "java-teens-system-out",
                "java-teens-class",
                "java-teens-class-2",
                "java-teens-class-3",
                "java-teens-scanner"
        ));
        blockMap.put(12, Arrays.asList(
                "java-teens-map-1",
                "java-teens-map-2",
                "java-teens-map-profession",
                "java-teens-init-dreams-method",
                "java-teens-process-1",
                "java-teens-to-lower-case",
                "java-teens-boolean",
                "java-teens-process-is-hello",
                "java-teens-compare-numbers",
                "java-teens-if-else"
        ));
        blockMap.put(13, Arrays.asList(
                "java-teens-process-2",
                "java-teens-array",
                "java-teens-iterate-map-keys",
                "java-teens-find-1",
                "java-teens-process-3",
                "java-teens-process-4",
                "java-teens-calculate-month-count",
                "java-teens-validate-month-count",
                "java-teens-calculate-month-count-2",
                "java-teens-final"
        ));

        //Generate hashes for blocks
        blockIndexHashes = new LinkedHashMap<>();
        blockMap.keySet().forEach(blockId -> {
            String hash = hashNamingService.getUniqueBlockHash(blockId);

            if (blockIndexHashes.containsKey(hash)) {
                throw new IllegalStateException("Duplicate hash for block " + blockId);
            } else {
                System.out.println("Block: " + blockId + ", hash: " + hash);
                blockIndexHashes.put(hash, blockId);
            }
        });

        //Debug blocks - one block one task
        singleTaskBlockMap = new LinkedHashMap<>();
        singleTaskBlockIndexHashes = new LinkedHashMap<>();

        loadSettings();
    }

    public void addSingleTaskAsDebugBlock(String taskId) {
        //Don't add if exists
        if (singleTaskBlockIndexHashes.containsKey(taskId)) {
            return;
        }

        int startIndex = 100;
        int maxIndex = startIndex + 10000;
        for(int freeIndex = startIndex; freeIndex < maxIndex; freeIndex++) {
            if (!singleTaskBlockMap.containsKey(freeIndex)) {
                //Ok, we find free index

                singleTaskBlockMap.put(freeIndex, Arrays.asList(taskId));
                singleTaskBlockIndexHashes.put(taskId, freeIndex);

                break;
            }
        }
    }

    public void loadSettings() {
        frontendUrl = globalPrefService.getOrDefault(FRONTEND_URL_PREF, "");
    }

    @Autowired
    private UserTaskStatService userTaskStatService;

    public Map<Integer, Integer> getUserBlockProgress(User user) {
        Map<Integer, Integer> result = new LinkedHashMap<>();

        List<String> userPassedTasks = userTaskStatService.getPassedTasks(user);

        blockMap.forEach((index, tasks) -> {
            int passedTaskCountInBlock = 0;
            for(String taskId: tasks) {
                if (userPassedTasks.contains(taskId)) {
                    passedTaskCountInBlock++;
                }
            }

            int percent = 100 * passedTaskCountInBlock / tasks.size();

            result.put(index, percent);
        });

        return result;
    }

    public List<Integer> getBlockIds() {
        return new ArrayList<>(blockMap.keySet());
    }

    public List<String> getBlockTasksIds(int blockNumber) {
        return blockMap.getOrDefault(blockNumber, singleTaskBlockMap.get(blockNumber));
    }

    public TaskBlockResponse createCurrentUserBlockState(User user, int blockIndex) {
        List<String> blockTasks = new ArrayList<>(getBlockTasksIds(blockIndex));
        List<String> passedTasks = new ArrayList<>();
        userTaskStatService.getPassedTasks(user).forEach(id -> {
            if (blockTasks.contains(id)) {
                passedTasks.add(id);
            }
        });

        TaskBlockResponse result = new TaskBlockResponse();
        result.setBlockIndex(blockIndex);
        result.setBlockTasks(blockTasks);
        result.setPassedTasks(passedTasks);

        for(String taskId: blockTasks) {
            if (!passedTasks.contains(taskId)) {
                result.setCurrentTask(taskId);

                int taskIndex = blockTasks.indexOf(taskId);
                if (taskIndex > 0) {
                    result.setCurrentTaskIndex(taskIndex + 1);
                } else {
                    result.setCurrentTaskIndex(1);
                }
                break;
            }
        }

        if (result.getCurrentTask() == null) {
            result.setCurrentTask(blockTasks.get(blockTasks.size() - 1));
        }

        return result;
    }

    public int getBlockIndexByHash(String hash) {
        return blockIndexHashes.getOrDefault(hash, singleTaskBlockIndexHashes.getOrDefault(hash, -1));
    }

    public String getHashByBlockIndex(int block) {
        for(String hash: blockIndexHashes.keySet()) {
            if (blockIndexHashes.get(hash) == block) {
                return hash;
            }
        }

        for(String hash: singleTaskBlockIndexHashes.keySet()) {
            if (singleTaskBlockIndexHashes.get(hash) == block) {
                return hash;
            }
        }

        return null;
    }

    public String generateBlockLinkForUser(User user, int block) {
        return generateBlockLinkForUser(user.getToken(), block);
    }

    public String generateBlockLinkForUser(String token, int block) {
        String link = frontendUrl + "?token=" + token + "&block=" + getHashByBlockIndex(block);

        String shortLink = cuttlyService.makeShortlink(link);
        if (shortLink == null) {
            return link;
        } else {
            return shortLink;
        }
    }

    public String generateLongLinkForUser(User user, int block) {
        return generateLongLinkForUser(user.getToken(), block);
    }

    public String generateLongLinkForUser(String token, int block) {
        return frontendUrl + "?token=" + token + "&block=" + getHashByBlockIndex(block);
    }

    public GlobalUserProgress getUserGlobalProgress(User user) {
        GlobalUserProgress result = new GlobalUserProgress();
        Map<Integer, Integer> dayProgress = getUserBlockProgress(user);

        boolean passedAllTasks = true;
        for(int singleDayProgress: dayProgress.values()) {
            if (singleDayProgress != 100) {
                passedAllTasks = false;
                break;
            }
        }

        if (passedAllTasks) {
            result.passedAllTasks = true;
            result.downloadArchiveLink = RESUME_LINK;
        }

        //Add links to other days only if day 5 finished
        if (dayProgress.getOrDefault(5, 0) == 100) {
            result.finishedLastDay = true;
        }

        String frontendUrl = "https://gomarathon.com.ua";
        String token = user.getToken();

        for(int day = 1; day <= 5; day++) {
            if (dayProgress.getOrDefault(day, 0) != 100) {
                String blockHash = getHashByBlockIndex(day);
                String link =  frontendUrl + "?token=" + token + "&block=" + blockHash;
                result.nonFinishedDays.put(day, link);
            }
        }

        return result;
    }

    @Data
    public static class GlobalUserProgress {
        private boolean success = true;

        private boolean finishedLastDay;
        private boolean passedAllTasks;
        private Map<Integer, String> nonFinishedDays = new LinkedHashMap<>();
        private String downloadArchiveLink = "";

        private List<String> finishedBlocks = new ArrayList<>();
    }

    public Map<String, BlockInfo> getBlocksProgress(User user, List<String> blocks) {
        Map<String, BlockInfo> result = new LinkedHashMap<>();

        List<String> userPassedTasks = userTaskStatService.getPassedTasks(user);

        blocks.forEach(blockHash -> {

            try {
                int blockIndex = blockIndexHashes.get(blockHash);
                List<String> taskIds = blockMap.get(blockIndex);

                BlockInfo blockInfo = new BlockInfo();

                blockInfo.taskIds = new ArrayList<>(taskIds);

                blockInfo.solvedTaskIds = new ArrayList<>();
                taskIds.forEach(taskId -> {
                    if (userPassedTasks.contains(taskId)) {
                        blockInfo.solvedTaskIds.add(taskId);
                    }
                });

                result.put(blockHash, blockInfo);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });

        return result;
    }

    @Data
    public static class BlockInfo {
        private List<String> taskIds;
        private List<String> solvedTaskIds;

        public int getProgress() {
            if (taskIds.size() <= 0) {
                return 0;
            }

            return 100 * solvedTaskIds.size() / taskIds.size();
        }
    }
}
