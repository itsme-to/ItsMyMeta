package ru.oftendev.itsmymeta.meta

import com.github.benmanes.caffeine.cache.Caffeine
import com.willfp.eco.core.config.interfaces.Config
import com.willfp.eco.core.data.ServerProfile
import com.willfp.eco.core.data.keys.PersistentDataKey
import com.willfp.eco.core.data.keys.PersistentDataKeyType
import com.willfp.eco.core.data.profile
import com.willfp.eco.core.placeholder.DynamicPlaceholder
import com.willfp.eco.core.placeholder.PlayerDynamicPlaceholder
import com.willfp.eco.core.placeholder.PlayerPlaceholder
import com.willfp.eco.core.placeholder.PlayerlessPlaceholder
import com.willfp.eco.core.registry.Registrable
import com.willfp.eco.util.NumberUtils
import com.willfp.eco.util.StringUtils
import com.willfp.eco.util.savedDisplayName
import com.willfp.eco.util.toNiceString
import org.bukkit.Bukkit
import org.bukkit.NamespacedKey
import org.bukkit.OfflinePlayer
import org.bukkit.block.CreatureSpawner
import org.bukkit.inventory.meta.BlockStateMeta
import ru.oftendev.itsmymeta.ItsMyMeta
import ru.oftendev.itsmymeta.commaFormat
import ru.oftendev.itsmymeta.fixedFormat
import ru.oftendev.itsmymeta.meta.enum.StatType
import ru.oftendev.itsmymeta.stringFormat
import ru.oftendev.itsmymeta.target.TargetType
import java.time.Duration
import java.util.UUID
import java.util.regex.Pattern

class Meta(
    val id: String,
    val type: StatType,
    val min: Double,
    val max: Double,
    val partyMode: TargetType,
    val isLocal: Boolean,
    var currency: MetaCurrency? = null,
    var default: Any = if (type == StatType.STRING) "" else 0.0,
): Registrable {
    private val topCacheTotal = Caffeine.newBuilder()
        .expireAfterWrite(Duration.ofSeconds(ItsMyMeta.instance
            .configYml.getInt("leaderboard-cache-lifetime").toLong()))
        .build<Int, LeaderboardCacheEntry?>()

    // val placeholderCache = mutableMapOf<UUID, Any>()

    val stats = mutableListOf<MetaStat>()

    var placeholder: String? = null

    private val saveId = if (isLocal) "${ItsMyMeta.instance.serverID}_${id}" else id

    val hasLeaderboard = this.type in listOf(StatType.DOUBLE, StatType.INTEGER)

    fun namespacedForPlayer(player: OfflinePlayer): NamespacedKey {
        return ItsMyMeta.instance.createNamespacedKey(
            "${this.partyMode.getParty(player).getUniqueId()}_${this.saveId.lowercase()}"
        )
    }

    val cachedValueKey = PersistentDataKey(
        ItsMyMeta.instance.createNamespacedKey("cached_${this.saveId.lowercase()}"),
        PersistentDataKeyType.STRING,
        ""
    )
    
    //

    private val posCacheTotal = Caffeine.newBuilder()
        .expireAfterWrite(Duration.ofSeconds(ItsMyMeta.instance
            .configYml.getInt("leaderboard-cache-lifetime").toLong()))
        .build<OfflinePlayer, Int>()

    private val posCachePercent = Caffeine.newBuilder()
        .expireAfterWrite(Duration.ofSeconds(ItsMyMeta.instance
            .configYml.getInt("leaderboard-cache-lifetime").toLong()))
        .build<OfflinePlayer, Double>()

    private val totalCache = Caffeine.newBuilder()
        .expireAfterWrite(Duration.ofSeconds(ItsMyMeta.instance
            .configYml.getInt("leaderboard-cache-lifetime").toLong()))
        .build<Int, Int>()

    private val totalAmountCache = Caffeine.newBuilder()
        .expireAfterWrite(Duration.ofSeconds(ItsMyMeta.instance
            .configYml.getInt("leaderboard-cache-lifetime").toLong()))
        .build<Int, Double>()

    constructor(config: Config): this(
        config.getString("id"),
        StatType.getById(config.getString("type"))!!,
        config.getDoubleOrNull("min") ?: Double.MIN_VALUE,
        if ((config.getDoubleOrNull("max") ?: Double.MAX_VALUE) <= 0) Double.MAX_VALUE
        else config.getDoubleOrNull("max") ?: Double.MAX_VALUE,
        config.getStringOrNull("team")?.let { TargetType.getById(config.getString("team")) }
            ?: TargetType.PLAYER,
        config.getBool("local")
    ) {
        placeholder = config.getStringOrNull("placeholder")
        currency = if (config.getBool("currency.enabled"))
            MetaCurrency(ItsMyMeta.instance, config.getSubsection("currency"), this)
        else null
        default = config.get("default") ?: if (type == StatType.STRING) "" else 0.0
        stats.addAll(
            config.getSubsections("statistics").mapNotNull {
                MetaStat(this, it, "Stat ${it.getString("id")}" +
                " or meta ${config.getString("id")}") }
        )
    }

    init {
        PlayerPlaceholder(
            ItsMyMeta.instance,
            id
        ) {
            it.getMeta(this).toString()
        }.register()

        PlayerPlaceholder(
            ItsMyMeta.instance,
            "${id}_fixed"
        ) {
            val v = it.getMeta(this)
            if (v is Number) {
                v.fixedFormat()
            } else v.toString()
        }.register()

        PlayerPlaceholder(
            ItsMyMeta.instance,
            "${id}_formatted"
        ) {
            val v = it.getMeta(this)
            if (v is Number) {
                v.stringFormat()
            } else v.toString()
        }.register()

        PlayerPlaceholder(
            ItsMyMeta.instance,
            "${id}_commas"
        ) {
            val v = it.getMeta(this)
            if (v is Number) {
                v.commaFormat()
            } else v.toString()
        }.register()

        PlayerPlaceholder(
            ItsMyMeta.instance,
            "${id}_name"
        ) {
            this.currency?.name ?: "None"
        }.register()

        PlayerPlaceholder(
            ItsMyMeta.instance,
            "${id}_default"
        ) {
            this.default.toString()
        }.register()

        PlayerPlaceholder(
            ItsMyMeta.instance,
            "${id}_max"
        ) {
            this.max.toNiceString()
        }.register()

        PlayerPlaceholder(
            ItsMyMeta.instance,
            "${id}_min"
        ) {
            this.min.toNiceString()
        }.register()

        DynamicPlaceholder(
            ItsMyMeta.instance,
            Pattern.compile("${this.id.lowercase()}_leaderboard_[0-9]+")
        ) {
            val result = it.split("_").reversed()[0].toInt()
            getTop(result)?.amount?.toNiceString() ?: ItsMyMeta.instance.langYml.getFormattedString("empty.score")
        }.register()

        DynamicPlaceholder(
            ItsMyMeta.instance,
            Pattern.compile("${this.id.lowercase()}_leaderboard_[0-9]+_name")
        ) {
            val result = it.split("_").reversed()[1].toInt()
            getTop(result)?.player?.savedDisplayName
                ?.toNiceString() ?: ItsMyMeta.instance.langYml.getFormattedString("empty.name")
        }.register()

        DynamicPlaceholder(
            ItsMyMeta.instance,
            Pattern.compile("${this.id.lowercase()}_leaderboard_[0-9]+_fixed")
        ) {
            val result = it.split("_").reversed()[1].toInt()
            getTop(result)?.amount?.toString()?.toDoubleOrNull()?.fixedFormat()
                ?: ItsMyMeta.instance.langYml.getFormattedString("empty.score")
        }.register()

        DynamicPlaceholder(
            ItsMyMeta.instance,
            Pattern.compile("${this.id.lowercase()}_leaderboard_[0-9]+_formatted")
        ) {
            val result = it.split("_").reversed()[1].toInt()
            getTop(result)?.amount?.toString()?.toDoubleOrNull()?.stringFormat()
                ?: ItsMyMeta.instance.langYml.getFormattedString("empty.score")
        }.register()

        DynamicPlaceholder(
            ItsMyMeta.instance,
            Pattern.compile("${this.id.lowercase()}_leaderboard_[0-9]+_commas")
        ) {
            val result = it.split("_").reversed()[1].toInt()
            getTop(result)?.amount?.toString()?.toDoubleOrNull()?.commaFormat()
                ?: ItsMyMeta.instance.langYml.getFormattedString("empty.score")
        }.register()

        PlayerPlaceholder(
            ItsMyMeta.instance,
            "${this.id.lowercase()}_leaderboard_position"
        ) { pl ->
            getTopForPlayer(pl).toString()
        }.register()

        PlayerPlaceholder(
            ItsMyMeta.instance,
            "${this.id.lowercase()}_leaderboard_position_percent"
        ) { pl ->
            getPercent(pl).toNiceString()
        }.register()
        
        PlayerlessPlaceholder(
            ItsMyMeta.instance,
            "${this.id.lowercase()}_leaderboard_total"
        ) {
            getTotal().toNiceString()
        }.register()

        PlayerlessPlaceholder(
            ItsMyMeta.instance,
            "${this.id.lowercase()}_leaderboard_total_players"
        ) {
            getTopAmount().toNiceString()
        }.register()
    }
    
    fun getTopForPlayer(player: OfflinePlayer): Int {
        if (this.type == StatType.STRING) return 0
        return posCacheTotal.get(player) {
            val players = Bukkit.getOfflinePlayers().sortedByDescending { it.getMeta(this)
                .toString().toDoubleOrNull() ?: 0.0 }

            players.indexOf(player) + 1
        }
    }

    fun getTop(place: Int): LeaderboardCacheEntry? {
        if (this.type == StatType.STRING) return null
        return topCacheTotal.get(place) {
            val players = Bukkit.getOfflinePlayers().sortedByDescending { it.getMeta(this)
                .toString().toDoubleOrNull() ?: 0.0 }
            val target = players.getOrNull(place-1) ?: return@get null
            LeaderboardCacheEntry(target, target.getMeta(this)
                .toString().toDoubleOrNull() ?: 0.0)
        }
    }

    fun getPercent(player: OfflinePlayer): Double {
        if (this.type == StatType.STRING) return 0.0
        return posCachePercent.get(player) {
            val top = getTopForPlayer(it)
            val total = getTopAmount()
            (top.toDouble()/total.toDouble())*100.0
        }
    }

    fun getTopAmount(): Int {
        if (this.type == StatType.STRING) return 0
        return totalCache.get(1) {
            when(this.type) {
                StatType.INTEGER -> Bukkit.getOfflinePlayers().filter {
                    (it.getMeta(this).toString().toDoubleOrNull()?.toInt() ?: 0) > (this.default.toString().toDoubleOrNull()?.toInt() ?: 0)
                }.size
                StatType.DOUBLE -> Bukkit.getOfflinePlayers().filter {
                    (it.getMeta(this).toString().toDoubleOrNull() ?: 0.0) > (this.default.toString().toDoubleOrNull() ?: 0.0)
                }.size
                else -> Bukkit.getOfflinePlayers().size
            }
        }
    }

    fun getTotal(): Double {
        if (this.type == StatType.STRING) return 0.0
        return totalAmountCache.get(1) {
            when(this.type) {
                StatType.INTEGER, StatType.DOUBLE -> Bukkit.getOfflinePlayers().sumOf {
                    it.getMeta(this).toString().toDoubleOrNull() ?: 0.0
            }
                else -> 0.0
            }
        }
    }

    override fun onRegister() {
        stats.forEach { it.bind() }
    }

    override fun onRemove() {
        stats.forEach { it.unbind() }
    }

    /**
     * Get the ID of the element.
     *
     * @return The ID.
     */
    override fun getID(): String {
        return id.lowercase()
    }
}

data class LeaderboardCacheEntry(
    val player: OfflinePlayer,
    val amount: Any
)

fun OfflinePlayer.giveMeta(meta: Meta, theValue: Any) {
    when(meta.type) {
        StatType.INTEGER -> {
            val current = this.getMeta(meta).toString().toDoubleOrNull()?.toInt() ?: 0
            val give = theValue.toString().toDoubleOrNull()?.toInt() ?: 0
            val result = (current+give).coerceIn(meta.min.toInt(), meta.max.toInt())
            this.setMeta(meta, result)
        }
        StatType.STRING -> {
            val give = theValue.toString()
            this.setMeta(meta, give)
        }
        else -> {
            val current = this.getMeta(meta).toString().toDoubleOrNull() ?: 0.0
            val give = theValue.toString().toDoubleOrNull() ?: 0.0
            val result = (current+give).coerceIn(meta.min, meta.max)
            this.setMeta(meta, result)
        }
    }
}

fun OfflinePlayer.setMeta(meta: Meta, theValue: Any) {
    meta.placeholder?.let {
        this.profile.write(meta.cachedValueKey, theValue.toString())
    }

    val sProfile = ServerProfile.load()
    val key = meta.namespacedForPlayer(this)
    when(meta.type) {
        StatType.INTEGER -> {
            sProfile.write(
                PersistentDataKey(
                    key,
                    PersistentDataKeyType.INT,
                    meta.default.toString().toDoubleOrNull()?.toInt() ?: 0
                ),
                theValue.toString().toDoubleOrNull()?.toInt() ?: 0
            )
        }
        StatType.STRING -> {
            sProfile.write(
                PersistentDataKey(
                    key,
                    PersistentDataKeyType.STRING,
                    meta.default.toString()
                ),
                theValue.toString()
            )
        }
        else -> {
            sProfile.write(
                PersistentDataKey(
                    key,
                    PersistentDataKeyType.DOUBLE,
                    meta.default.toString().toDoubleOrNull() ?: 0.0
                ),
                theValue.toString().toDoubleOrNull() ?: 0.0
            )
        }
    }
}

fun OfflinePlayer.takeMeta(meta: Meta, theValue: Any) {
    when(meta.type) {
        StatType.INTEGER -> {
            val current = this.getMeta(meta).toString().toDoubleOrNull()?.toInt() ?: 0
            val give = theValue.toString().toDoubleOrNull()?.toInt() ?: 0
            val result = (current-give).coerceIn(meta.min.toInt(), meta.max.toInt())
            this.setMeta(meta, result)
        }
        StatType.STRING -> {
            val give = theValue.toString()
            this.setMeta(meta, give)
        }
        else -> {
            val current = this.getMeta(meta).toString().toDoubleOrNull() ?: 0.0
            val give = theValue.toString().toDoubleOrNull() ?: 0.0
            val result = (current-give).coerceIn(meta.min, meta.max)
            this.setMeta(meta, result)
        }
    }
}

fun OfflinePlayer.resetMeta(meta: Meta) {
    this.setMeta(meta, meta.default)
}

fun OfflinePlayer.canUse(meta: Meta): Boolean {
    return meta.partyMode.getParty(this).isApplicableFor(this)
}

fun OfflinePlayer.getMeta(meta: Meta): Any {
    meta.placeholder?.let {
        return if (player == null) {
            this.profile.read(meta.cachedValueKey)
        } else NumberUtils.evaluateExpression(meta.placeholder!!, player)
    }

    val sProfile = ServerProfile.load()
    val key = meta.namespacedForPlayer(this)
    return when(meta.type) {
        StatType.INTEGER -> {
            sProfile.read(
                PersistentDataKey(
                    key,
                    PersistentDataKeyType.INT,
                    meta.default.toString().toDoubleOrNull()?.toInt() ?: 0
                )
            )
        }
        StatType.STRING -> {
            sProfile.read(
                PersistentDataKey(
                    key,
                    PersistentDataKeyType.STRING,
                    meta.default.toString()
                )
            )
        }
        else -> {
            sProfile.read(
                PersistentDataKey(
                    key,
                    PersistentDataKeyType.DOUBLE,
                    meta.default.toString().toDoubleOrNull() ?: 0.0
                )
            )
        }
    }
}

fun getOfPlayer(name: String): OfflinePlayer? {
    val entry = Bukkit.getOfflinePlayer(name)
    return if (!entry.hasPlayedBefore() && !entry.isOnline) null else entry
}

