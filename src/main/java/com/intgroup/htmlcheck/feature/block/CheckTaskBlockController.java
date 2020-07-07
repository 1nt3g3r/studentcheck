package com.intgroup.htmlcheck.feature.block;

import com.intgroup.htmlcheck.controller.api.RequestResult;
import com.intgroup.htmlcheck.domain.security.User;
import com.intgroup.htmlcheck.service.security.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.List;
import java.util.StringJoiner;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v2/taskblock")
public class CheckTaskBlockController {
    @Autowired
    private UserService userService;

    @Autowired
    private CheckTaskBlockService checkTaskBlockService;

    @GetMapping("/getTotalProgress")
    public Object getTotalProgress(@RequestParam(name = "token", required = false, defaultValue = "") String token) {
        User user = userService.findUserByToken(token);

        if (user == null) {
            return RequestResult.fail("Invalid token");
        }

        return checkTaskBlockService.getUserGlobalProgress(user);
    }

    @GetMapping("/getBlocksProgress")
    public Object getBlocksProgress(@RequestParam(name = "token", required = false, defaultValue = "") String token,
                                   @RequestParam(name = "blocks", required = false) String blocks) {
        User user = userService.findUserByToken(token);

        if (user == null) {
            return RequestResult.fail("Invalid token");
        }

        if (blocks == null || blocks.isBlank()) {
            return RequestResult.fail("Blocks hashes should not be empty");
        }

        List<String> blockHashes = Arrays.stream(blocks.split(",")).filter(block -> !block.isBlank()).collect(Collectors.toList());
        if (blockHashes.isEmpty()) {
            return RequestResult.fail("Blocks hashes should not be empty");
        }

        return checkTaskBlockService.getBlocksProgress(user, blockHashes);
    }

    @GetMapping("/get")
    public Object getCurrentBlock(@RequestParam(name = "token", required = false, defaultValue = "") String token,
                                  @RequestParam(name = "block", required = false, defaultValue = "") String block) {
        User user = userService.findUserByToken(token);

        if (user == null) {
            return RequestResult.fail("Invalid token");
        }

        int blockIndex = checkTaskBlockService.getBlockIndexByHash(block);
        if (blockIndex < 0) {
            return RequestResult.fail("Invalid block index");
        }

        return checkTaskBlockService.createCurrentUserBlockState(user, blockIndex);
    }
}
