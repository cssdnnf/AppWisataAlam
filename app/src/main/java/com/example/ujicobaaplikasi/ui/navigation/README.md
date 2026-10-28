# BottomNavView — template navigasi bawah

Komponen floating bottom navigation yang terpisah dari layar mana pun. Bisa
dipakai di Activity, Fragment, atau project Android lain dengan sedikit
penyesuaian.

## File yang perlu disalin

Kotlin:

- `BottomNavView.kt`
- `BottomNavDestination.kt`

Resource:

- `res/layout/view_bottom_navigation.xml`
- `res/drawable/bg_nav_item.xml`
- `res/drawable/ic_nav_home.xml`
- `res/drawable/ic_nav_explore.xml`
- `res/drawable/ic_nav_saved.xml`
- `res/drawable/ic_nav_profile.xml`
- `res/color/nav_item_tint.xml`
- `res/color/nav_item_label.xml`
- string `nav_home`, `nav_explore`, `nav_saved`, `nav_profile`
- warna `hijau_lombok`, `hijau_lembut`, `nav_nonaktif`, `nav_ripple`,
  `permukaan`, `garis_lembut`, `white`, `transparent`

Font memakai `@font/plus_jakarta_sans`. Bila project tujuan tidak memakai font
itu, ganti `fontFamily` di layout atau hapus atributnya.

## Cara pakai

Letakkan komponen di bawah konten. Jangan memakai `fitsSystemWindows` pada
layout host karena komponen sudah menangani inset bawah. Host tetap perlu
menangani inset atas (misalnya padding pada kontainer konten) agar header tidak
tertutup status bar. Beri komponen background yang sama dengan halaman
(`android:background="@color/...")` supaya konten tidak terlihat di sela kartu
nav.

```xml
<LinearLayout
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:orientation="vertical">

    <FrameLayout
        android:id="@+id/content"
        android:layout_width="match_parent"
        android:layout_height="0dp"
        android:layout_weight="1" />

    <com.example.ujicobaaplikasi.ui.navigation.BottomNavView
        android:id="@+id/bottomNav"
        android:layout_width="match_parent"
        android:layout_height="wrap_content" />
</LinearLayout>
```

Pasang listener di host:

```kotlin
bottomNav.selectedDestination = BottomNavDestination.HOME
bottomNav.setOnDestinationSelectedListener { destination ->
    when (destination) {
        BottomNavDestination.HOME -> showHome()
        BottomNavDestination.EXPLORE -> showExplore()
        BottomNavDestination.SAVED -> showSaved()
        BottomNavDestination.PROFILE -> showProfile()
    }
}
```

`selectedDestination = ...` hanya mengubah visual. Callback juga terpanggil
saat pengguna mengetuk ulang tab yang sedang aktif, sehingga layar detail dapat
memakai tab yang sama untuk kembali ke shell utama. Di host tab, abaikan event
itu bila tidak diperlukan (lihat pengecekan `currentTab` di `MainActivity`).

Di layar non-tab (detail, pengaturan), arahkan setiap tab kembali ke host
utama agar state navigasi tetap sinkron. Contoh ada di
`ui/main/MainActivity.kt` pada method `open`.

## Menyesuaikan

- Ganti label: ubah string `nav_*` atau panggil `setLabel(destination, "Label")`.
- Ganti ikon: ubah `iconRes` di `BottomNavDestination` dan file vektornya.
- Tambah tab: tambahkan enum, item layout, warna, dan cabang navigasi di host.
- Warna aktif memakai `hijau_lombok`; ubah `nav_item_tint.xml`,
  `nav_item_label.xml`, dan `bg_nav_item.xml` untuk brand lain.
- Inset bawah (gesture/navigation bar) ditangani otomatis oleh
  `BottomNavView.applyBottomSystemInset()`.

Catatan: template ini memakai MaterialCardView dan Material Components
(`com.google.android.material:material`). Tidak membutuhkan Navigation
Component.
