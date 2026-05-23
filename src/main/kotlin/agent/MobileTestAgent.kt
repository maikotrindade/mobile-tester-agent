@file:OptIn(ExperimentalTime::class)

package agent

import agent.model.MobileTesterConfig
import agent.strategy.TestingStrategy
import agent.tool.mobile.test.MobileTestTools
import agent.tool.mobile.test.ReportingTools
import ai.koog.agents.core.agent.AIAgent
import ai.koog.agents.core.agent.GraphAIAgent
import ai.koog.agents.core.agent.config.AIAgentConfig
import ai.koog.agents.core.feature.handler.agent.AgentStartingContext
import ai.koog.agents.core.tools.ToolRegistry
import ai.koog.agents.features.eventHandler.feature.handleEvents
import ai.koog.prompt.dsl.prompt
import ai.koog.prompt.params.LLMParams
import kotlinx.coroutines.CompletableDeferred
import kotlin.time.ExperimentalTime

object MobileTestAgent {
    private var config: MobileTesterConfig = MobileTesterConfig()

    suspend fun runAgent(goal: String, packageName: String, steps: List<String>): String {
        val appPackage = packageName
        val resultDeferred = CompletableDeferred<String>()

        val agentConfig = AIAgentConfig(
            prompt = prompt("mobileTester", LLMParams(temperature = config.llmTemperature)) {
                system(
                    """
                    You are an autonomous Android UI testing agent. You drive a real device/emulator via ADB tools
                    to execute a list of test steps and report which ones passed or failed.

                    LIFECYCLE (strict):
                      1. First action: call startTestingScenario(appPackage) ONCE, passing the EXACT
                         `App package` value given in the user message — do not guess or modify it.
                         This connects ADB and launches the target app via `am`/monkey. If it returns OK,
                         the app is already on screen — do NOT tap any launcher icon, app drawer, or
                         home screen entry to open it. If it returns ERROR, stop and FAIL the scenario;
                         do not try to open the app manually.
                      2. Execute every step in order, one step at a time, starting from the app's current screen.
                      3. Final action: call closeApp once.
                      4. After closeApp, emit ONE assistant message summarising each step as PASS or FAIL with a one-line reason.

                    PER-STEP LOOP (act → verify → recover):
                      a) Act: take the smallest tool action that advances the step.
                      b) Verify: call verifyElementVisible (or verifyElementNotVisible for transitions) to confirm the
                         expected state. Never assume an action worked from its return string alone.
                      c) Recover if verify fails:
                           - If NOT_VISIBLE, try swipeUp / swipeDown / wait(500), then verify again.
                           - If NOT_FOUND, vary the selector (try synonyms, case, partial text) or switch selectorType.
                           - If AMBIGUOUS, retry tap with position=1, 2, … — do NOT re-search with different text.
                         Do not repeat the same failing action more than TWICE. After that, mark the step FAIL and continue.

                    SELECTOR STRATEGY (most to least reliable):
                      resource-id > content-desc > text. Pass selectorType when you know which attribute applies.
                      Default "any" searches all three.

                    TOOL RESULT CONVENTION:
                      Every tool returns a string starting with one of: OK | TAPPED | VISIBLE | NOT_VISIBLE |
                      NOT_FOUND | AMBIGUOUS | ERROR. Parse this prefix to decide next action.

                    TIMING:
                      Call wait(300..1500) after navigation, app launch, or animations. Never tap blind right after
                      a screen transition.

                    SCREENSHOTS:
                      Take a screenshot at the end of each step for evidence, and on any FAIL. Do not screenshot
                      mid-action.

                    ANTI-PATTERNS — DO NOT:
                      - Call startTestingScenario more than once — it is the FIRST action only.
                      - Tap an app icon / launcher / home screen to open the target app. The app is launched
                        programmatically via ADB inside startTestingScenario.
                      - Call closeApp between steps.
                      - Loop the same failing tool call > 2x in a row.
                      - Skip verification.
                      - Invent selectors not seen on screen — call findUiElementsByText or getScreenDump first.

                    TERMINATION:
                      Stop only after closeApp + the final summary message. Format:
                        Step 1: PASS — <one-line reason>
                        Step 2: FAIL — <one-line reason>
                        ...
                    """.trimIndent()
                )
            },
            model = config.executorInfo.llmModel,
            maxAgentIterations = config.maxAgentIterations
        )

        val toolRegistry = ToolRegistry {
            tools(MobileTestTools())
            tools(ReportingTools())
        }

        val agent = AIAgent(
            promptExecutor = config.executorInfo.executor,
            strategy = TestingStrategy.strategy,
            agentConfig = agentConfig,
            toolRegistry = toolRegistry
        ) {
            handleEvents {
                onAgentStarting { eventContext: AgentStartingContext ->
                    println("Starting strategy: ${(eventContext.agent as? GraphAIAgent<*, *>)?.strategy?.name ?: eventContext.agent.id}")
                }

                onToolCallStarting { eventContext ->
                    println(
                        "Tool called: tool ${eventContext.toolName}, args ${eventContext.toolArgs}"
                    )
                }

                onAgentExecutionFailed { eventContext ->
                    println("An error occurred: ${eventContext.error.message}\n${eventContext.error.stackTraceToString()}")
                }

                onAgentCompleted { eventContext ->
                    resultDeferred.complete(
                        if (eventContext.result != null) {
                            "onAgentFinished: ${eventContext.result.toString()}"
                        } else {
                            "Something went wrong."
                        }
                    )
                }

            }
        }

        val testScenario = buildString {
            appendLine("Goal: $goal")
            appendLine("App package (use this exact value for startTestingScenario): $appPackage")
            appendLine("Steps:")
            steps.forEachIndexed { idx, step ->
                appendLine("Step ${idx + 1}. $step")
            }
        }

        agent.run(testScenario)
        return resultDeferred.await()
    }

    fun updateConfiguration(newConfig: MobileTesterConfig) {
        config = newConfig
    }
}