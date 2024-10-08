package cn.weigui.qdds.service

import cn.weigui.qdds.model.BaseDataModel
import cn.weigui.qdds.model.BaseModel
import cn.weigui.qdds.model.BaseRewardModel
import cn.weigui.qdds.model.CardCallModel
import cn.weigui.qdds.model.CardCallPageModel
import cn.weigui.qdds.model.CheckInDetailModel
import cn.weigui.qdds.model.ExchangeChapterCardModel
import cn.weigui.qdds.model.LotteryModel
import cn.weigui.qdds.model.MascotTaskModel
import cn.weigui.qdds.model.RiskConfModel
import cn.weigui.qdds.model.WelfareCenterModel
import cn.weigui.qdds.util.customModelRequest
import com.skydoves.sandwich.ApiResponse
import io.ktor.client.HttpClient

interface QdService {

    suspend fun checkRisk(model: BaseModel<BaseDataModel>): ApiResponse<BaseModel<BaseRewardModel>>

    suspend fun getWelfareCenter(model: BaseModel<BaseDataModel>): ApiResponse<BaseModel<WelfareCenterModel>>

    suspend fun getWelfareReward(
        model: BaseModel<BaseDataModel>
    ): ApiResponse<BaseModel<BaseRewardModel>>

    suspend fun receiveWelfareReward(model: BaseModel<BaseDataModel>): ApiResponse<BaseModel<BaseRewardModel>>

    suspend fun checkInDetail(model: BaseModel<BaseDataModel>): ApiResponse<BaseModel<CheckInDetailModel>>

    suspend fun autoCheckIn(model: BaseModel<BaseDataModel>): ApiResponse<String>

    suspend fun normalCheckIn(model: BaseModel<BaseDataModel>): ApiResponse<String>

    suspend fun lotteryChance(model: BaseModel<BaseDataModel>): ApiResponse<String>

    suspend fun lottery(model: BaseModel<BaseDataModel>): ApiResponse<BaseModel<LotteryModel>>

    suspend fun exchangeChapterCard(model: BaseModel<BaseDataModel>): ApiResponse<BaseModel<ExchangeChapterCardModel>>

    suspend fun buyChapterCard(model: BaseModel<BaseDataModel>): ApiResponse<String>

    suspend fun gameTime(model: BaseModel<BaseDataModel>): ApiResponse<String>

    suspend fun getCardCallPage(model: BaseModel<BaseDataModel>): ApiResponse<BaseModel<CardCallPageModel>>

    suspend fun getCardCall(model: BaseModel<BaseDataModel>): ApiResponse<BaseModel<CardCallModel>>

    suspend fun getMascotTaskList(model: BaseModel<BaseDataModel>): ApiResponse<BaseModel<MascotTaskModel>>

    suspend fun getMascotClockIn(model: BaseModel<BaseDataModel>): ApiResponse<BaseModel<RiskConfModel>>

    suspend fun getMascotReward(model: BaseModel<BaseDataModel>): ApiResponse<BaseModel<RiskConfModel>>

}

class QdServiceImpl(
    private val httpClient: HttpClient
) : QdService {

    override suspend fun checkRisk(model: BaseModel<BaseDataModel>): ApiResponse<BaseModel<BaseRewardModel>> =
        model.data?.customModelRequest(httpClient) ?: throw Exception(model.message)

    override suspend fun getWelfareCenter(model: BaseModel<BaseDataModel>): ApiResponse<BaseModel<WelfareCenterModel>> =
        model.data?.customModelRequest(httpClient) ?: throw Exception(model.message)

    override suspend fun getWelfareReward(
        model: BaseModel<BaseDataModel>
    ): ApiResponse<BaseModel<BaseRewardModel>> =
        model.data?.customModelRequest(httpClient) ?: throw Exception(model.message)

    override suspend fun receiveWelfareReward(model: BaseModel<BaseDataModel>): ApiResponse<BaseModel<BaseRewardModel>> =
        model.data?.customModelRequest(httpClient) ?: throw Exception(model.message)

    override suspend fun checkInDetail(model: BaseModel<BaseDataModel>): ApiResponse<BaseModel<CheckInDetailModel>> =
        model.data?.customModelRequest(httpClient) ?: throw Exception(model.message)

    override suspend fun autoCheckIn(model: BaseModel<BaseDataModel>): ApiResponse<String> =
        model.data?.customModelRequest(httpClient) ?: throw Exception(model.message)

    override suspend fun normalCheckIn(model: BaseModel<BaseDataModel>): ApiResponse<String> =
        model.data?.customModelRequest(httpClient) ?: throw Exception(model.message)

    override suspend fun lotteryChance(model: BaseModel<BaseDataModel>): ApiResponse<String> =
        model.data?.customModelRequest(httpClient) ?: throw Exception(model.message)

    override suspend fun lottery(model: BaseModel<BaseDataModel>): ApiResponse<BaseModel<LotteryModel>> =
        model.data?.customModelRequest(httpClient) ?: throw Exception(model.message)

    override suspend fun exchangeChapterCard(model: BaseModel<BaseDataModel>): ApiResponse<BaseModel<ExchangeChapterCardModel>> =
        model.data?.customModelRequest(httpClient) ?: throw Exception(model.message)

    override suspend fun buyChapterCard(model: BaseModel<BaseDataModel>): ApiResponse<String> =
        model.data?.customModelRequest(httpClient) ?: throw Exception(model.message)

    override suspend fun gameTime(model: BaseModel<BaseDataModel>): ApiResponse<String> =
        model.data?.customModelRequest(httpClient) ?: throw Exception(model.message)

    override suspend fun getCardCallPage(model: BaseModel<BaseDataModel>): ApiResponse<BaseModel<CardCallPageModel>> =
        model.data?.customModelRequest(httpClient) ?: throw Exception(model.message)

    override suspend fun getCardCall(model: BaseModel<BaseDataModel>): ApiResponse<BaseModel<CardCallModel>> =
        model.data?.customModelRequest(httpClient) ?: throw Exception(model.message)

    override suspend fun getMascotTaskList(model: BaseModel<BaseDataModel>): ApiResponse<BaseModel<MascotTaskModel>> =
        model.data?.customModelRequest(httpClient) ?: throw Exception(model.message)

    override suspend fun getMascotClockIn(model: BaseModel<BaseDataModel>): ApiResponse<BaseModel<RiskConfModel>> =
        model.data?.customModelRequest(httpClient) ?: throw Exception(model.message)

    override suspend fun getMascotReward(model: BaseModel<BaseDataModel>): ApiResponse<BaseModel<RiskConfModel>> =
        model.data?.customModelRequest(httpClient) ?: throw Exception(model.message)

}