# Developer Guide

Some Things You Must Know.

## Database

Please note there are three datasources for three profiles.
Also note the `dev.com`, putting the domain name in your hosts file.

- default

    ```yaml
    spring:
      datasource:
        driver-class-name: org.postgresql.Driver
        url: jdbc:postgresql://dev.com:5432/bicarb_dev
        username: bicarb
        password: bicarb
    ```

- test

    ```yaml
    spring:
      datasource:
        url: jdbc:postgresql://dev.com:5432/bicarb_test
        username: bicarb
        password: bicarb
    ```

- production

    ```yaml
    spring:
      datasource:
        url: jdbc:postgresql://localhost:5432/bicarb
        username: bicarb
        password: bicarb
    ```

## CSRF

CSRF protection is enabled, don't forget to include it in your request.

```html
<th:block th:if="${_csrf}">
  <meta name="_csrf" content="" th:content="${_csrf.token}">
  <meta name="_csrf_header" content="" th:content="${_csrf.headerName}">
  <meta name="_csrf_parameter" content="" th:content="${_csrf.parameterName}">
</th:block>
```

## Content Security Policy

CSP is enabled for `index.html` template. By default, inline scripts is forbidden,
you can specify a whitelist using a cryptographic nonce.

```html
<script th:attr="nonce=${nonce}" th:inline="javascript">
  // do something
</script>
```

## Error Code

All defined error codes.

- ConflictException(409x)
  - user
    - 4091: username conflict
    - 4092: email conflict
    - 4093: nickname conflict
  - category
    - 4094: category slug conflict
- 422x
  - 4221(update password): incorrect confirmPassword
