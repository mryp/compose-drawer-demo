package dev.mryp.drawerdemo

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.Folder
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import dev.mryp.drawerdemo.ui.theme.DrawerDemoTheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch


//------------------------------------------------------------------
//クラス
//------------------------------------------------------------------
/**
 * メイン画面アクティビティ
 */
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ThemeScreen()
        }
    }
}


//------------------------------------------------------------------
//画面UI関数
//------------------------------------------------------------------
/**
 * ルート画面
 */
@Composable
private fun ThemeScreen() {
    DrawerDemoTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colors.background
        ) {
            ScaffoldScreen()
        }
    }
}

/**
 * ナビゲーション・ドロワーを備えた全体画面
 */
@Composable
private fun ScaffoldScreen() {
    val scope = rememberCoroutineScope()
    val scaffoldState = rememberScaffoldState(rememberDrawerState(DrawerValue.Closed))
    val navController = rememberNavController()
    //val context = LocalContext.current as MainActivity

    Scaffold(
        scaffoldState = scaffoldState,
        topBar = {
            TopBar(
                scope = scope,
                scaffoldState = scaffoldState,
                navController = navController
            )
        },
        drawerContent = {
            Drawer(
                scope = scope,
                scaffoldState = scaffoldState,
                navController = navController
            )
        }
    ) {
        Navigation(
            navController = navController
        )
    }
}

/**
 * 上部のタイトル・アクションバー
 */
@Composable
private fun TopBar(
    scope: CoroutineScope,
    scaffoldState: ScaffoldState,
    navController: NavController
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route ?: stringResource(R.string.app_name)

    TopAppBar(
        title = { Text(text = getTopBarTitle(currentRoute)) },
        navigationIcon = {
            IconButton(onClick = {
                scope.launch {
                    scaffoldState.drawerState.open()
                }
            }) {
                Icon(Icons.Filled.Menu, "")
            }
        }
    )
}

/**
 * ドロワーアイテムオブジェクトクラス
 */
private sealed class NavDrawerItem(
    var route: String,
    var icon: ImageVector,
    var title: String
) {
    object Folder : NavDrawerItem("folder", Icons.Outlined.Folder, "フォルダ")
    object Favorite : NavDrawerItem("favorite", Icons.Outlined.FavoriteBorder, "お気に入り")
    object History : NavDrawerItem("history", Icons.Outlined.History, "履歴")
    object Setting : NavDrawerItem("setting", Icons.Outlined.Settings, "設定")
}

/**
 * ナビゲーションドロワーに表示する項目リスト
 */
private val naviItemList: List<NavDrawerItem> = listOf(
    NavDrawerItem.Folder,
    NavDrawerItem.Favorite,
    NavDrawerItem.History,
    NavDrawerItem.Setting
)

/**
 * タイトルバーのタイトル文字列を現在の画面文字列から取得する
 */
private fun getTopBarTitle(route: String): String {
    for (item in naviItemList) {
        if (route == item.route) {
            return item.title
        }
    }

    return ""
}

/**
 * ナビゲーションドロワー画面
 */
@Composable
private fun Drawer(
    scope: CoroutineScope,
    scaffoldState: ScaffoldState,
    navController: NavController
) {
    val items = naviItemList
    Column {
        val navBackStackEntry by navController.currentBackStackEntryAsState()
        val currentRoute = navBackStackEntry?.destination?.route

        //ヘッダ
        Text(
            text = "Demo",
            textAlign = TextAlign.Center,
            fontWeight = FontWeight.Bold,
            modifier = Modifier
                .padding(16.dp)
                .align(Alignment.CenterHorizontally)
        )
        Divider()

        //メニュー項目
        items.forEach { item ->
            DrawerItem(item = item, selected = currentRoute == item.route, onItemClick = {
                navController.navigate(item.route) {
                    navController.graph.startDestinationRoute?.let { route ->
                        popUpTo(route) {
                            saveState = true
                        }
                    }
                    launchSingleTop = true
                    restoreState = true
                }
                scope.launch {
                    scaffoldState.drawerState.close()
                }
            })
        }
    }
}

/**
 * ナビゲーションドロワーの項目画面
 */
@Composable
private fun DrawerItem(
    item: NavDrawerItem,
    selected: Boolean,
    onItemClick: (NavDrawerItem) -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = { onItemClick(item) })
            .height(48.dp)
            .padding(start = 8.dp)
    ) {
        Icon(
            item.icon,
            modifier = Modifier.padding(start = 8.dp),
            contentDescription = item.title
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = item.title,
            fontSize = 18.sp,
        )
    }
}

/**
 * 遷移処理対応の中身画面
 */
@Composable
private fun Navigation(
    navController: NavHostController
) {
    NavHost(navController, startDestination = NavDrawerItem.Folder.route) {
        composable(NavDrawerItem.Folder.route) {
            FolderScreen()
        }
        composable(NavDrawerItem.History.route) {
            HistoryScreen()
        }
        composable(NavDrawerItem.Favorite.route) {
            FavoriteScreen()
        }
        composable(NavDrawerItem.Setting.route) {
            SettingScreen()
        }
    }
}

/**
 * お気に入り画面
 */
@Composable
private fun FolderScreen() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .wrapContentSize(Alignment.Center)
    ) {
        Text(
            text = "フォルダ",
            fontWeight = FontWeight.Bold,
            modifier = Modifier.align(Alignment.CenterHorizontally),
            textAlign = TextAlign.Center,
            fontSize = 25.sp
        )
    }
}

/**
 * お気に入り画面
 */
@Composable
private fun FavoriteScreen() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .wrapContentSize(Alignment.Center)
    ) {
        Text(
            text = "お気に入り",
            fontWeight = FontWeight.Bold,
            modifier = Modifier.align(Alignment.CenterHorizontally),
            textAlign = TextAlign.Center,
            fontSize = 25.sp
        )
    }
}

/**
 * 履歴画面
 */
@Composable
private fun HistoryScreen() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .wrapContentSize(Alignment.Center)
    ) {
        Text(
            text = "履歴",
            fontWeight = FontWeight.Bold,
            modifier = Modifier.align(Alignment.CenterHorizontally),
            textAlign = TextAlign.Center,
            fontSize = 25.sp
        )
    }
}

/**
 * 設定画面
 */
@Composable
private fun SettingScreen() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .wrapContentSize(Alignment.Center)
    ) {
        Text(
            text = "設定",
            fontWeight = FontWeight.Bold,
            modifier = Modifier.align(Alignment.CenterHorizontally),
            textAlign = TextAlign.Center,
            fontSize = 25.sp
        )
    }
}

//------------------------------------------------------------------
//プレビューUI関数
//------------------------------------------------------------------
@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    ThemeScreen()
}