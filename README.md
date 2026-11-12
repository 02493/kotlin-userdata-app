# Unit 1 - User Data App

## Суть приложения

Android-приложение для ввода и хранения данных пользователя с возможностью просмотра информации об устройстве, батарее, местоположении, событиях календаря и взаимодействия с системными приложениями.

## Основные возможности

- **FragmentContainerView** в качестве корневого элемента MainActivity
- **DataStore** для хранения данных пользователя (ключи доступа: `user_name` и `user_age`)
- **Экран входа (LoginFragment)** с полями для ввода имени и возраста через TextInputLayout/TextInputEditText
- **Кнопка Login** сохраняет данные в DataStore и переключается на главный экран
- **Главный экран (HomeFragment)** с информацией о пользователе, устройстве, уровне заряда батареи, Advertising ID
- **Кнопки на главном экране**: Create email, Open Search Engine, View Events, View Location, Exit
- **ContentProvider** для доступа к данным из DataStore
- **BroadcastReceiver** для отслеживания подключения/отключения зарядного устройства с вибрацией
- **Список событий календаря** с RecyclerView, DiffUtil, запросом разрешения на доступ к календарю
- **Фрагмент местоположения** с FusedLocationProviderClient, отображением координат, кнопкой "View on map"
- **Переход на стартовый/главный фрагмент** через FragmentManager в зависимости от наличия данных

## Структура проекта

- `MainActivity.kt` - главная активность с FragmentContainerView, автоматический переход
- `UserDataStore.kt` - класс для работы с DataStore
- `LoginFragment.kt` - стартовый экран входа
- `HomeFragment.kt` - главный экран
- `EventsListFragment.kt` - экран списка событий календаря
- `LocationFragment.kt` - экран местоположения
- `CalendarEvent.kt` - модель события календаря
- `CalendarEventAdapter.kt` - RecyclerView.Adapter
- `CalendarEventDiffCallback.kt` - DiffUtil
- `ChargerReceiver.kt` - BroadcastReceiver для зарядки
- `UserDataContentProvider.kt` - ContentProvider для доступа к DataStore

## Инструкция для запуска

1. Открыть проект в Android Studio
2. Дождаться синхронизации Gradle
3. Запустить приложение на эмуляторе или физическом устройстве (API 24+)
4. На первом экране ввести имя и возраст, нажать Login
5. Данные сохранятся в DataStore, произойдет переход на главный экран
6. При наличии разрешений можно просматривать события календаря и местоположение
7. Доступ к данным возможен через ContentProvider (content://com.example.unit1.provider/user_data)

