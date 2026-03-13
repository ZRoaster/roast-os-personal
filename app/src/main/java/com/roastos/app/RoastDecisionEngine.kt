package com.roastos.app

data class RoastDecision(
    val stage: String,
    val priority: String,
    val heatAction: String,
    val airflowAction: String,
    val flavorDirection: String,
    val rationale: String,
    val confidence: String
)

object RoastDecisionEngine {

    fun evaluate(
        snapshot: RoastSessionBusSnapshot
    ): RoastDecision {

        val session = snapshot.session
        val beanTemp = session.lastBeanTemp
        val ror = session.lastRor
        val elapsed = session.lastElapsedSec
        val phase = snapshot.companion.phaseLabel
        val validation = snapshot.validation
        val control = RoastControlModel.evaluate()

        if (session.status != RoastSessionStatus.RUNNING) {
            return applyControlModel(
                RoastDecision(
                    stage = phase,
                    priority = "待机",
                    heatAction = "无",
                    airflowAction = "无",
                    flavorDirection = "未开始",
                    rationale = "当前没有进行中的烘焙。",
                    confidence = "高"
                ),
                control
            )
        }

        val topIssue = validation.issues.firstOrNull()

        if (topIssue != null) {
            val base = when (topIssue.code) {

                "stall" -> RoastDecision(
                    stage = phase,
                    priority = "恢复动能",
                    heatAction = "小幅加火",
                    airflowAction = "风量暂不继续加大",
                    flavorDirection = "避免发闷，保住中段支撑",
                    rationale = "当前 RoR 偏低，存在失速风险。优先恢复热动能，避免后段塌陷。",
                    confidence = severityToConfidence(topIssue.severity)
                )

                "crash" -> RoastDecision(
                    stage = phase,
                    priority = "防止尾段塌陷",
                    heatAction = "不要继续减火过快",
                    airflowAction = "保持稳定",
                    flavorDirection = "避免尾段空、薄、平",
                    rationale = "后段 RoR 掉得过快，若继续失去支撑，杯中后段可能发空。",
                    confidence = severityToConfidence(topIssue.severity)
                )

                "flick" -> RoastDecision(
                    stage = phase,
                    priority = "控制后段过冲",
                    heatAction = "停止继续推火",
                    airflowAction = "保持或轻微增风",
                    flavorDirection = "避免尖、躁、收尾粗",
                    rationale = "后段 RoR 反弹偏强，继续推进会让收尾变尖。",
                    confidence = severityToConfidence(topIssue.severity)
                )

                "low_energy" -> RoastDecision(
                    stage = phase,
                    priority = "补足中段能量",
                    heatAction = "轻微补火",
                    airflowAction = "避免风门过大",
                    flavorDirection = "维持结构感与甜感基础",
                    rationale = "中段能量不足，后续可能出现结构变薄。",
                    confidence = severityToConfidence(topIssue.severity)
                )

                "high_energy" -> RoastDecision(
                    stage = phase,
                    priority = "抑制推进过猛",
                    heatAction = "收一点火",
                    airflowAction = "视情况轻微增风",
                    flavorDirection = "避免粗糙，保留细致度",
                    rationale = "中段推进偏强，若不收，容易影响后续风味精细度。",
                    confidence = severityToConfidence(topIssue.severity)
                )

                else -> buildBaseDecision(
                    phase = phase,
                    beanTemp = beanTemp,
                    ror = ror,
                    elapsed = elapsed
                )
            }

            return applyControlModel(base, control)
        }

        val base = buildBaseDecision(
            phase = phase,
            beanTemp = beanTemp,
            ror = ror,
            elapsed = elapsed
        )

        return applyControlModel(base, control)
    }

    fun buildDisplayText(
        snapshot: RoastSessionBusSnapshot
    ): String {
        val d = evaluate(snapshot)

        return """
阶段
${d.stage}

当前重点
${d.priority}

火力建议
${d.heatAction}

风门建议
${d.airflowAction}

风味走向
${d.flavorDirection}

判断依据
${d.rationale}

可信度
${d.confidence}
        """.trimIndent()
    }

    private fun buildBaseDecision(
        phase: String,
        beanTemp: Double,
        ror: Double,
        elapsed: Int
    ): RoastDecision {
        return when (phase) {

            "Charge" -> RoastDecision(
                stage = phase,
                priority = "建立回温节奏",
                heatAction = "保持当前火力",
                airflowAction = "保持基础风量",
                flavorDirection = "建立干净开局",
                rationale = "当前处于吸热初段，优先观察回温与起势，不急于干预。",
                confidence = buildConfidence(ror, elapsed)
            )

            "Drying" -> {
                if (ror < 4.0) {
                    RoastDecision(
                        stage = phase,
                        priority = "避免脱水段偏弱",
                        heatAction = "轻微补火",
                        airflowAction = "维持当前风量",
                        flavorDirection = "避免后段失去支撑",
                        rationale = "脱水段 RoR 偏低，后续中段可能变薄。",
                        confidence = buildConfidence(ror, elapsed)
                    )
                } else {
                    RoastDecision(
                        stage = phase,
                        priority = "维持平稳推进",
                        heatAction = "保持当前火力",
                        airflowAction = "保持稳定",
                        flavorDirection = "建立清晰结构",
                        rationale = "脱水节奏正常，当前更重要的是保持平稳。",
                        confidence = buildConfidence(ror, elapsed)
                    )
                }
            }

            "Maillard" -> {
                if (ror < 4.0) {
                    RoastDecision(
                        stage = phase,
                        priority = "维持中段支撑",
                        heatAction = "小幅补火",
                        airflowAction = "避免过度加风",
                        flavorDirection = "保留甜感与厚度基础",
                        rationale = "梅纳段 RoR 偏低，需防止结构塌薄。",
                        confidence = buildConfidence(ror, elapsed)
                    )
                } else if (ror > 10.0) {
                    RoastDecision(
                        stage = phase,
                        priority = "避免中段过冲",
                        heatAction = "收一点火",
                        airflowAction = "轻微增风",
                        flavorDirection = "保留细致度",
                        rationale = "梅纳段推进过快，可能牺牲后续风味精细度。",
                        confidence = buildConfidence(ror, elapsed)
                    )
                } else {
                    RoastDecision(
                        stage = phase,
                        priority = "维持下降曲线",
                        heatAction = "平稳控火",
                        airflowAction = "稳定风量",
                        flavorDirection = "建立甜感与层次",
                        rationale = "当前中段推进均衡，重点是保持下降而不失速。",
                        confidence = buildConfidence(ror, elapsed)
                    )
                }
            }

            "First Crack" -> RoastDecision(
                stage = phase,
                priority = "进入发展段判断",
                heatAction = "不要再明显推火",
                airflowAction = "保持稳定或轻微增风",
                flavorDirection = "开始决定清晰感或甜感取向",
                rationale = "进入一爆附近后，重点转向发展控制与收尾节奏。",
                confidence = buildConfidence(ror, elapsed)
            )

            "Development" -> {
                if (beanTemp >= 200.0 && ror <= 3.0) {
                    RoastDecision(
                        stage = phase,
                        priority = "控制收尾不要发闷",
                        heatAction = "避免继续减得过快",
                        airflowAction = "保持干净排气",
                        flavorDirection = "保留尾韵活性",
                        rationale = "发展段后半 RoR 偏低，需防止收尾过重。",
                        confidence = buildConfidence(ror, elapsed)
                    )
                } else {
                    RoastDecision(
                        stage = phase,
                        priority = "决定最终落点",
                        heatAction = "小幅微调即可",
                        airflowAction = "保持稳定",
                        flavorDirection = "根据目标选择清晰或甜感",
                        rationale = "发展段重点不是大动作，而是精确决定落点。",
                        confidence = buildConfidence(ror, elapsed)
                    )
                }
            }

            else -> RoastDecision(
                stage = phase,
                priority = "观察",
                heatAction = "保持",
                airflowAction = "保持",
                flavorDirection = "待判断",
                rationale = "当前阶段信息有限，先观察走势。",
                confidence = buildConfidence(ror, elapsed)
            )
        }
    }

    private fun applyControlModel(
        decision: RoastDecision,
        control: RoastControlAdjustment
    ): RoastDecision {
        val adjustedHeat = appendHeatBias(
            base = decision.heatAction,
            bias = control.recommendedHeatBiasPercent
        )

        val adjustedAirflow = appendAirflowBias(
            base = decision.airflowAction,
            bias = control.recommendedAirflowBiasSteps
        )

        val adjustedRationale = """
${decision.rationale}

控制模型修正
Heat Bias: ${formatSignedPercent(control.recommendedHeatBiasPercent)}
Airflow Bias: ${formatSignedStep(control.recommendedAirflowBiasSteps)}
原因: ${control.reason}
        """.trimIndent()

        return decision.copy(
            heatAction = adjustedHeat,
            airflowAction = adjustedAirflow,
            rationale = adjustedRationale
        )
    }

    private fun appendHeatBias(
        base: String,
        bias: Int
    ): String {
        return if (bias == 0) {
            "$base（Heat Bias 0%）"
        } else {
            "$base（Heat Bias ${formatSignedPercent(bias)}）"
        }
    }

    private fun appendAirflowBias(
        base: String,
        bias: Int
    ): String {
        return if (bias == 0) {
            "$base（Airflow Bias 0）"
        } else {
            "$base（Airflow Bias ${formatSignedStep(bias)}）"
        }
    }

    private fun formatSignedPercent(
        value: Int
    ): String {
        return if (value >= 0) "+${value}%" else "${value}%"
    }

    private fun formatSignedStep(
        value: Int
    ): String {
        return if (value >= 0) "+$value" else "$value"
    }

    private fun buildConfidence(
        ror: Double,
        elapsed: Int
    ): String {
        return when {
            elapsed < 20 -> "中"
            ror < 1.5 -> "中"
            else -> "高"
        }
    }

    private fun severityToConfidence(
        severity: String
    ): String {
        return when (severity) {
            "high" -> "高"
            "medium" -> "高"
            "watch" -> "中"
            "low" -> "中"
            else -> "中"
        }
    }
}
