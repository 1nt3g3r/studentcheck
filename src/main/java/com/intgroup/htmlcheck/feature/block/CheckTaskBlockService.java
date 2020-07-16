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

        //Module 1
        blockMap.put(1, Arrays.asList("js-hw-1-1", "js-hw-1-2", "js-hw-1-3", "js-hw-1-4", "js-hw-1-5"));
        
        //Module 2
        blockMap.put(2, Arrays.asList("js-hw-2-1", "js-hw-2-2", "js-hw-2-3", "js-hw-2-4", "js-hw-2-5", "js-hw-2-6", "js-hw-2-7", "js-hw-2-8", "js-hw-2-9"));
        
        //Module 3
        blockMap.put(3, Arrays.asList("js-hw-3-1", "js-hw-3-2", "js-hw-3-3", "js-hw-3-4", "js-hw-3-5", "js-hw-3-6"));
        
        //Module 4
        blockMap.put(4, Arrays.asList("js-hw-4-1", "js-hw-4-2", "js-hw-4-3", "js-hw-4-4", "js-hw-4-5"));

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

        int startIndex = 1000;
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
