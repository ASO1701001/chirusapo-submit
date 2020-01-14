package jp.ac.asojuku.st.chirusapo

import io.realm.RealmObject
import io.realm.annotations.PrimaryKey
import io.realm.annotations.Required

open class JoinGroup : RealmObject() {
    @PrimaryKey
    open var Rgroup_id: String = ""
    @Required
    open var Rgroup_name: String = ""
    open var Rpin_code: String = "0000"
    open var Rgroup_flag: Int = 0
}