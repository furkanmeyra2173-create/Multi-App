# Multi-App

Android 10–16 hedefli çoklu uygulama ve Magisk yönetim projesi.

## İlk sürüm
- Android uygulama iskeleti
- GitHub Actions ile debug APK derlemesi
- Magisk entegrasyonu için başlangıç ekranı

## Teknik not
Magisk'in gerçek root motoru Android'in boot sürecine entegre edilir; yalnızca APK içine gömülmesi root sağlamaz. Multi-App, desteklenen bir Magisk kurulumunun yönetimini kendi arayüzünden sunacak şekilde geliştirilecektir. Boot imajı işlemleri cihaz modeli ve Android sürümüne göre değişir.

## Derleme
GitHub Actions sekmesinden **Build Multi-App APK** iş akışını çalıştır. Başarılı derlemeden sonra `Multi-App-debug` artifact dosyasını indir.
