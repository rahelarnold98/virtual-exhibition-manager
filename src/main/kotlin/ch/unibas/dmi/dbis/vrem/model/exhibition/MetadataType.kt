package ch.unibas.dmi.dbis.vrem.model.exhibition

enum class MetadataType(val key: String = "") {

    GENERATED("generated"),
    OBJECT_ID("objectId"),
    MEMBER_IDS("memberIds"),
    SEED("seed")

}
