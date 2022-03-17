package xyz.linglitel.unzipl

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.DocumentsContract
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.documentfile.provider.DocumentFile
import com.permissionx.guolindev.PermissionX
import java.io.File
import java.io.FileOutputStream
import kotlin.concurrent.thread


class MainActivity : AppCompatActivity() {

    private val ANDROID_DATA_DOCUMENT_ID = "primary:Android/data"
    private val EXTERNAL_STORAGE_PROVIDER_AUTHORITY =
        "com.android.externalstorage.documents"
    private val androidDataTreeUri = DocumentsContract.buildTreeDocumentUri(
        EXTERNAL_STORAGE_PROVIDER_AUTHORITY,
        ANDROID_DATA_DOCUMENT_ID
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        PermissionX.init(this)
            .permissions(Manifest.permission.WRITE_EXTERNAL_STORAGE,Manifest.permission.READ_EXTERNAL_STORAGE)
            .request { allGranted, grantedList, deniedList ->
                if (allGranted) {
                    Toast.makeText(this, "所有申请的权限都已通过", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, "您拒绝了如下权限：$deniedList", Toast.LENGTH_SHORT).show()
                }
            }
        setContentView(R.layout.activity_main)
        val p = findViewById<Button>(R.id.p)
        if (!isDataGrant()){
            p.setText("无权限,请点击申请")
        }else{
            p.setText("权限申请成功")
        }
        p.setOnClickListener {
            startForDataPermission(this)
        }
        val qq = findViewById<Button>(R.id.qq)
        qq.setOnClickListener {
                val intent = Intent()
                intent.setData(Uri.parse("mqqopensdkapi://bizAgent/qm/qr?url=http%3A%2F%2Fqm.qq.com%2Fcgi-bin%2Fqm%2Fqr%3Ffrom%3Dapp%26p%3Dandroid%26jump_from%3Dwebapi%26k%3DUJbEtTLXlGXg83uZJY_-3lCzq5iEjUyN"))
                try {
                    startActivity(intent)
                } catch (e : Exception) {
                }

        }
        val u = findViewById<Button>(R.id.z)
        u.setOnClickListener {
            val intent = Intent()
            intent.setClassName(
                "ru.zdevs.zarchiver.pro",
                "ru.zdevs.zarchiver.pro.ZArchiver"
            )
            try {
                startActivity(intent)
            }catch (e : Exception){
                Toast.makeText(this,"无法跳转,您是否安装了它?",Toast.LENGTH_SHORT).show()
            }

        }
        var filename1: String
        val mutableList : ArrayList<String> = ArrayList()
        val s = findViewById<Button>(R.id.s)
        val f = findViewById<Button>(R.id.f)
        val zz = findViewById<TextView>(R.id.zz)
        s.setOnClickListener {
            val filename = findViewById<EditText>(R.id.filename)
            filename1 = filename.text.toString()
            for (file in filename1.split(",")){
                mutableList.add(file)
            }
            zz.append("已解析${filename1.split(",").size}个\n")
            s.text = "解析成功"
        }
        f.setOnClickListener {
            thread {
                for ( file in mutableList){
                    val fo = getDoucmentFile("/storage/emulated/0/Android/data/com.tencent.mobileqq/Tencent/QQfile_recv/${f}")
                    val o = contentResolver.openInputStream(fo!!.getUri())
                    val fso = File("/Movies/EVA")
                    if (!fso.exists()){
                        fso.mkdirs()
                    }
                    val fos = FileOutputStream("/sdcard/Movies/EVA/${f}")
                    val buf = ByteArray(2048)
                    var len: Int
                    while (o!!.read(buf).also { len = it } > 0) {
                        fos.write(buf, 0, len)
                    }
                    fos.close()
                    o.close()
                    fos.close()
                    zz.append("复制${file}成功\n")

                }
                zz.append("所有文件复制完成,请手动检查")
                zz.append("文件成功复制到/Movies/EVA/")
                f.text = "复制成功"
            }
        }

    }

    fun startForDataPermission(activity: Activity) {
        Intent(Intent.ACTION_OPEN_DOCUMENT_TREE).apply {
            flags = Intent.FLAG_GRANT_READ_URI_PERMISSION or
                    Intent.FLAG_GRANT_WRITE_URI_PERMISSION or
                    Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION or
                    Intent.FLAG_GRANT_PREFIX_URI_PERMISSION
            putExtra(
                DocumentsContract.EXTRA_INITIAL_URI,
                DocumentFile.fromTreeUri(activity, androidDataTreeUri)?.uri
            )
        }.also {
            activity.startActivityForResult(it, 1)
            Toast.makeText(this,isDataGrant().toString(),Toast.LENGTH_SHORT).show()
        }
    }

    //根据路径获得document文件
    fun getDoucmentFile(path: String): DocumentFile? {
        var path = path
        if (path.endsWith("/")) {
            path = path.substring(0, path.length - 1)
        }
        val path2 = path.replace("/storage/emulated/0/", "").replace("/", "%2F")
        return DocumentFile.fromSingleUri(
            this,
            Uri.parse("content://com.android.externalstorage.documents/tree/primary%3AAndroid%2Fdata/document/primary%3A$path2")
        )
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        var uri: Uri?
        if (data == null) {
            return
        }
        val uriTree = data.data
        if (requestCode == 1 && data.data.also { uri = it } != null) {
            contentResolver.takePersistableUriPermission(
                uriTree!!, Intent.FLAG_GRANT_READ_URI_PERMISSION
                        or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
            )
        }
        Toast.makeText(this,isDataGrant().toString(),Toast.LENGTH_SHORT).show()
        if (isDataGrant()){
            val p = findViewById<Button>(R.id.p)
            p.setText("权限申请成功")
            p.setOnClickListener {
                startForDataPermission(this)
            }
        }
    }

    fun isDataGrant(): Boolean {
        for (persistedUriPermission in contentResolver.persistedUriPermissions) {
            if ((persistedUriPermission.uri == androidDataTreeUri) &&
                persistedUriPermission.isWritePermission &&
                persistedUriPermission.isReadPermission
            ) {
                return true
            }
        }
        return false
    }
}