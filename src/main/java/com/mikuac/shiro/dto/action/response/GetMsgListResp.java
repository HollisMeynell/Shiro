package com.mikuac.shiro.dto.action.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
public class GetMsgListResp {

    @JsonProperty("messages")
    private List<MsgResp> messages;

}
