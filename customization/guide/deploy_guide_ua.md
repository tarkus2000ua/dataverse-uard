> Historical supplied instructions for a Dataverse 6.6 HTMX guide.
> The current Dataverse 6.12 integration uses complete static chapter pages and
> the native :NavbarGuidesUrl setting. Follow
> [the maintained local guide documentation](../../docs/local-user-guide.md)
> instead of the HelpUrl/custom-header steps below.

# Інструкція з розгортання українського посібника користувача Dataverse

Цей документ містить вказівки для адміністраторів системи Dataverse (наприклад, **nsr.dntb.gov.ua**) щодо того, як замінити стандартні англомовні посібники на локалізовану українську версію 6.6.

## 1. Підготовка файлів

Переконайтеся, що всі файли посібника зібрані в одній папці (наприклад, `guide/`).
Структура має бути такою:
```text
guide/
├── index.html
├── welcome.html
├── account.html
├── find-use-data.html
├── dataverse-management.html
├── dataset-management.html
├── tabular-data-ingest.html
└── appendix.html
```

## 2. Хостинг (Розміщення на сервері)

Найпростіший спосіб — розмістити цю папку безпосередньо на вашому веб-сервері (наприклад, **Nginx** або **Apache**), який обслуговує основний домен.

Приклад для Nginx:
1. Скопіюйте папку до `/var/www/html/guide/`.
2. Тепер посібник доступний за адресою `https://your-domain.gov.ua/guide/index.html`.

## 3. Налаштування Dataverse (Help URL)

Щоб кнопка "Help" у верхньому меню Dataverse вела на вашу українську версію, необхідно змінити налаштування в базі даних через API.

Виконайте наступну команду на сервері (замініть `your-domain` на ваш реальний домен):

```bash
curl -X PUT -d "https://your-domain.gov.ua/guide/index.html" http://localhost:8080/api/admin/settings/:HelpUrl
```

*Примітка: Якщо ваш API захищений, додайте хедер API-KEY.*

## 4. Випадок із захардкодженим посиланням (Custom Header)

Якщо після виконання команди вище посилання в меню не змінилося, це означає, що воно прописане вручну в файлі **custom-header.html**.

Адміністратору необхідно:
1. Знайти файл `custom-header.html` на сервері (зазвичай у директорії, вказаній у налаштуванні `:StyleCustomizationFile`).
2. Знайти в ньому наступний фрагмент:
   ```html
   <li><a href="https://guides.dataverse.org/en/6.6/user" rel="noopener" target="_blank">Довідник користувача</a></li>
   ```
3. Замінити атрибут `href` на адресу вашого нового посібника:
   ```html
   <li><a href="/guide/index.html" rel="noopener" target="_blank">Довідник користувача</a></li>
   ```
4. Після збереження просто перезавантажте сторінку в браузері.

## 5. Технічні особливості (SPA та HTMX)

Ця версія посібника реалізована як **Single Page Application (SPA)** за допомогою бібліотеки **HTMX**.
- Вона автоматично підвантажує контент без перезавантаження всієї сторінки.
- Вона використовує **Bootstrap 3.4.1**, що забезпечує ідеальну візуальну сумісність з інтерфейсом Dataverse v6.6.
- Усі посилання на зображення ведуть на офіційні сервери `guides.dataverse.org` за абсолютними шляхами, тому вам не потрібно переносити самі картинки.

## 6. Підтримка та оновлення

Для оновлення тексту достатньо просто замінити відповідний `.html` файл у папці `guide/` на сервері. Зміни вступлять в силу миттєво без перезавантаження сервісу Dataverse (Glassfish/Payara).
