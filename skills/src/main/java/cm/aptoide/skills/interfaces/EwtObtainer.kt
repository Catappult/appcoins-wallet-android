package cm.aptoide.skills.interfaces

import io.reactivex.Single

interface EwtObtainer {

  fun getEWT(): Single<String>
}