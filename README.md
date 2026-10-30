# Multi-App

Android 10–16 hedefli çoklu uygulama ve Magisk yönetim projesi.

## Şu anki özellikler
- Android uygulama iskeleti ve koyu arayüz
- GitHub Actions ile debug APK derlemesi
- MagiskSU üzerinden gerçek root erişimi kontrolü
- Magisk motoru sürüm/yol bilgisi kontrolü
- `/data/adb/modules` altındaki modülleri listeleme
- ZIP modülü seçme ve Magisk'in resmi `--install-module` komutuyla yükleme
- Modülleri devre dışı bırakma/etkinleştirme
- Modülü sonraki açılışta kaldırılmak üzere işaretleme

## Magisk mimarisi ve sınırlar
Magisk'in motoru Android'in boot sürecine entegre edilir; yalnızca bir APK içine kopyalanması root sağlamaz. Bu sürüm, cihazda zaten çalışan Magisk motoruna `su` üzerinden bağlanan uygulama içi bir yönetim panelidir. Root/Magisk yoksa bu panelin yönetim işlemleri çalışmaz.

Magisk resmi olarak Android 7.0 ve üzerini destekler; bu proje Android 10–16 aralığını hedefler. Cihaz üreticisi, kernel, boot image ve Magisk sürümüne göre uyumluluk değişebilir.

Modül ZIP dosyaları root yetkisiyle kod çalıştırabilir. Yalnızca güvendiğin modülleri yükle. Modül kaldırma işlemi dosyayı anında silmez; Magisk'e sonraki yeniden başlatmada kaldırılacağını bildirir.

## Magisk lisansı
Magisk, GNU General Public License v3 veya (tercihe bağlı olarak) daha yeni bir sürüm altında yayımlanan açık kaynaklı bir projedir. Bu uygulama şu an Magisk'in motor veya kaynak kodunu kendi APK'sına dahil etmez; kurulu Magisk komut satırı arayüzüyle iletişim kurar. Resmî kaynak: https://github.com/topjohnwu/Magisk

## Derleme
GitHub Actions sekmesinden **Build Multi-App APK** iş akışını çalıştır. Başarılı derlemeden sonra `Multi-App-debug` artifact dosyasını indir.
