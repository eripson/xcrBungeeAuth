# xcrBungeeAuth

Plugin odpowiada za wymuszenie logowania dla graczy z kontem non-premium w zależności od ustawienia w configu, zapisywanie historii logowań oraz śledzenie adresów IP gracza.

## Co i jak?

Pluginy udostępniam, bo i tak leżą na dysku a w MC nie będę się już bawić. Wszystko działało z wersjami 1.7/1.8, jednak, gdy ktoś znajdzie jakiegoś buga niech zrobi zgłoszenie i dokładnie go opisze. Na wszelkie nowe funkcjonalności robicie Pull request'a, jak ktoś je sprawdzi to wtedy zostaną dodane. Wszystkie nowości zmieniające rozgrywkę najlepiej jak będą możliwe do wyłączenia w configu.

Z pluginów możecie korzystać w jakikolwiek sposób chcecie, kod możecie modyfikować do woli, możecie się z niego uczyć jak bardzo to Wam pomoże. Jedyne o co się uprasza to:
1. Nie podpisywanie się jako autor opublikowanych pluginów (nawet po drobnych modyfikacjach).
2. Wszystko co może w jakikolwiek sposób poprawić opublikowany projekt zgłaszajcie/poprawajcie etc.

## Opis pliku konfiguracyjnego

| Klucz | Wartość | Opis |
| ----- | ------- | ---- |
| sessions-enabled | wartość logiczna (boolean) | Określa, czy sesje graczy mają być włączone (wartość **true**) czy też nie - wartość **false**. |
| sessions-time | czas (HH:MM:SS) | Długość sesji gracza wyrażona w formacie GODZINY:MINUTY:SEKUNDY. Określa, przez jak długi czas od ostatniego logowania się gracza non-premium może on wejść ponownie na serwer bez konieczności logowania się. Ustawienie parametru `sessions-enabled` na false powoduje ignorowanie tego ustawienia. |
| auth-server | tekst (string) | Nazwa serwera (wzięta z konfiguracji **BungeeCorda**), który jest podłączony do BungeeCorda, na który będą wysyłani gracze non-premium w celu zalogowania się. |
| mysql.host | tekst (string) | Adres IP serwera MySQL, z którym ma połączyć się plugin. Dla serwera MySQL postawionego na maszynie lokalnej wpisujemy `localhost` bądź `127.0.0.1`. |
| mysql.base | tekst (string) | Nazwa bazy danych, której będzie używał plugin. |
| mysql.user | tekst (string) | Nazwa użytkownika (z dostępem do ww. bazy). Jako ten użytkownik plugin będzie łączył się z bazą i wykonywał wszelakie operacje na danych. |
| mysql.pass | tekst (string) | Hasło do konta ww. użytkownika. |

Ze względu na bezpieczeństwo **stanowczo odradzam** wpisywania do configu danych konta **root** serwera MySQL.

#### Wygląd przykładowego pliku konfiguracyjnego

```yaml
config:
  sessions-enabled: false
  sessions-time: 00:00:00
  auth-server: logowanie
  mysql:
    host: "localhost"
    base: "xcr"
    user: "xcrafters"
    pass: "xcrafters123"
```

## Omówienie komend

| Komenda | Wymagane uprawnienie | Opis działania |
| ------- | -------------------- | -------------- |
| /auth register `nick` `hasło` | auth.manage + auth.register | Rejestruje konto gracza o nicku `nick` z poziomu administratora. Argument `hasło` (którego podanie nie jest obowiązkowe), ustawi graczowi określone hasło. |
| /auth unregister `nick` | auth.manage + auth.unregister | Usuwa konto gracza o nicku `nick`. Zwięźlej mówiąc - wyrejestrowuje go. |
| /auth changepass `nick` `hasło` | auth.manage + auth.changepass | Zmienia graczowi o nicku `nick` haslo na `haslo`. |
| /auth premium `nick` | auth.manage + auth.premium | Przełącza graczowi o nicku `nick` sposób logowania się pomiędzy logowaniem premium, a logowaniem non-premium. |
| /auth mode offline/online | auth.manage + auth.mode | Zmienia tryb logowania się wszystkim graczom. Ustawienie offline pozwoli wejść wszystkim na serwer, natomiast ustawienie online - tylko osobom posiadającym legalną kopię gry (premium). |
| /auth multi `nick`/`ip` | auth.manage + auth.multi | Pokazuje wszystkie multikonta zarejestrowane przez gracza o nicku `nick` lub wszystkie konta zarejestrowane z IP `ip`. |
| /auth ip `nick` | auth.manage + auth.ip | Pokazuje wszystkie adresy IP, z których logował się gracz. |
| /auth track `nick`/`ip` | auth.manage + auth.track | Pokazuje ostatnie logowania dla gracza o nicku `nick` lub z adresu IP `ip`. |
| /auth reload | auth.manage + auth.reload | Przeładowuje plik konfiguracyjny pluginu. |
||||
| /changepassword `hasło` `powtórzenie` | brak | Zmienia hasło dla gracza non-premium na `hasło` (które musi zostać potwierdzone w argumencie `powtórzenie`. |
| /login `hasło` | brak | Loguje gracza non-premium do gry, jeśli hasło podane w argumencie `hasło` zgadza się z rzeczywistym hasłem ustalonym podczas rejestracji. |
| /register `hasło` `powtórzenie` `kod z mapy` | brak | Rejestruje nowe konto gracza non-premium. Ustawia hasło do konta na `hasło` (które musi zgadzać się z argumentem `powtórzenie`) i sprawdza, czy kod z mapy dodanej do ekwipunku gracza zgadza się z tym podanym w argumencie `kod z mapy`. |

Pierwsza część komend w tabeli to komendy administracyjne, druga natomiast - komendy graczy.

## TODO
- [x] Napisanie README do pluginu (lista komend, jak używać configu, etc.)
- [ ] Sprawdzenie czy wszystko działa jak należy
