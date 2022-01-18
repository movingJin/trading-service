package com.tradingbot.tradingservice.setting.domain;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper
public interface TradeSettingMapper {
    TradeSettingMapper INSTANCE = Mappers.getMapper(TradeSettingMapper.class);

    /**
     * @return {@link TradeSetting}
     */

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "uuid", ignore = true)
    TradeSetting toEntity(TradeSettingDto tradeSettingDto);

    TradeSettingDto toDto(TradeSetting tradeSetting);
}
