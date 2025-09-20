package com.example.xrpl.user.api;

import java.util.List;
import java.util.Map;

public interface UserQueryService {

    Map<Long, String> findWalletAddressesByUserIds(List<Long> userIds);
}
