# Auto-Resolve Report — PR #6

> **PR originale** : #6 — *Test analyzer conflict*
> **Branche base** : `main` | **Branche source** : `test-analyzer-conflict`
> **Généré par** : Code Auditor v7.3 (Interactive + RAG-enhanced resolution)

## Résumé des résolutions

| Fichier | Méthode | Conflits | Détails |
|---|---|---|---|
| `src/main/java/tn/esprit/sampleprojet/UserRepository.java` | `interactive_llm` | 16 | 10 auto, 6 interactif |
| `src/main/java/tn/esprit/sampleprojet/UserService.java` | `interactive_llm` | 31 | 21 auto, 10 interactif |

## Détails des résolutions

### `UserRepository.java`

**Bloc 1** — Type: `simple` | Résolution: ✅

**Bloc 2** — Type: `import` | Résolution: ✅

**Bloc 3** — Type: `import` | Résolution: ✅

**Bloc 4** — Type: `simple` | Résolution: ✅

**Bloc 5** — Type: `simple` | Résolution: ✅

**Bloc 6** — Type: `other` | Résolution: 🤖 LLM

<details><summary>OURS (preview)</summary>

```
        // Changement de la logique de hashage pour créer un conflit de logique
>         return "SHA256_" + plainPassword.trim().toLowerCase();
> 
```
</details>

<details><summary>THEIRS (preview)</summary>

```
        return "hashed_" + plainPassword; // Example placeholder
> 
```
</details>

**Bloc 7** — Type: `other` | Résolution: 🤖 LLM

<details><summary>OURS (preview)</summary>

```
        // Modification de la requête SQL (ajout d'un alias ou changement d'espacement)
>         String sql = "SELECT u.id, u.username, u.email FROM us
```
</details>

<details><summary>THEIRS (preview)</summary>

```
        String sql = "SELECT id, username, email FROM users WHERE id = ?";
> 
```
</details>

**Bloc 8** — Type: `simple` | Résolution: ✅

**Bloc 9** — Type: `simple` | Résolution: ✅

**Bloc 10** — Type: `simple` | Résolution: ✅

**Bloc 11** — Type: `method` | Résolution: 🤖 LLM

<details><summary>THEIRS (preview)</summary>

```
    public List<User> findAll() throws SQLException {
>         List<User> users = new ArrayList<>();
>         String sql = "SELECT id, username, email F
```
</details>

**Bloc 12** — Type: `other` | Résolution: 🤖 LLM

<details><summary>OURS (preview)</summary>

```
        // Modification de l'ordre des colonnes dans le INSERT
>         String sql = "INSERT INTO users (email, username, password) VALUES (?, ?, ?)";
> 
```
</details>

<details><summary>THEIRS (preview)</summary>

```
        String sql = "INSERT INTO users (username, email, password) VALUES (?, ?, ?)";
> 
```
</details>

**Bloc 13** — Type: `other` | Résolution: 🤖 LLM

<details><summary>OURS (preview)</summary>

```
            pstmt.setString(1, user.email);
>             pstmt.setString(2, user.username);
>             pstmt.setString(3, hashPassword(user.getPasswor
```
</details>

<details><summary>THEIRS (preview)</summary>

```
            pstmt.setString(1, user.username);
>             pstmt.setString(2, user.email);
>             pstmt.setString(3, hashPassword(user.getPasswor
```
</details>

**Bloc 14** — Type: `simple` | Résolution: ✅

**Bloc 15** — Type: `other` | Résolution: 🤖 LLM

<details><summary>OURS (preview)</summary>

```
        // Changement de la requête de COUNT(*) à COUNT(1)
>         String sql = "SELECT COUNT(1) AS total_count FROM users";
> 
```
</details>

<details><summary>THEIRS (preview)</summary>

```
        // PROBLEM 15: Multiple resource leaks (fixed by try-with-resources)
>         // PROBLEM 16: Neither Statement nor ResultSet closed! (fixed by 
```
</details>

**Bloc 16** — Type: `simple` | Résolution: ✅

### `UserService.java`

**Bloc 1** — Type: `import` | Résolution: ✅

**Bloc 2** — Type: `import` | Résolution: ✅

**Bloc 3** — Type: `import` | Résolution: ✅

**Bloc 4** — Type: `simple` | Résolution: ✅

**Bloc 5** — Type: `simple` | Résolution: ✅

**Bloc 6** — Type: `other` | Résolution: 🤖 LLM

<details><summary>OURS (preview)</summary>

```
    private static final String ADMIN_PASSWORD = "admin123!";
> 
```
</details>

<details><summary>THEIRS (preview)</summary>

```
    @Autowired
>     public UserService(DataSource dataSource) {
>         this.dataSource = dataSource;
>     }
> 
```
</details>

**Bloc 7** — Type: `other` | Résolution: 🤖 LLM

<details><summary>OURS (preview)</summary>

```
        // BUG INTENTIONNEL: SQL Injection
>         String query = "SELECT * FROM users WHERE username = '" + username + "'";
> 
```
</details>

<details><summary>THEIRS (preview)</summary>

```

>                 // All fields required for a complete User object (as per User constructor) should be retrieved.
>                 String query = "SELE
```
</details>

**Bloc 8** — Type: `simple` | Résolution: ✅

**Bloc 9** — Type: `simple` | Résolution: ✅

**Bloc 10** — Type: `other` | Résolution: 🤖 LLM

<details><summary>OURS (preview)</summary>

```
                    return mapUser(rs);
> 
```
</details>

<details><summary>THEIRS (preview)</summary>

```

>                     int id = rs.getInt("id");
>                     String retrievedUsername = rs.getString("username");
>                     String pas
```
</details>

**Bloc 11** — Type: `other` | Résolution: 🤖 LLM

<details><summary>OURS (preview)</summary>

```
        if (username.equals("admin") && password.equals(ADMIN_PASSWORD)) {
>             return true;  // BUG: backdoor admin
>         }
> 
```
</details>

**Bloc 12** — Type: `simple` | Résolution: ✅

**Bloc 13** — Type: `simple` | Résolution: ✅

**Bloc 14** — Type: `other` | Résolution: 🤖 LLM

<details><summary>OURS (preview)</summary>

```
                    return hashPassword(password).equals(rs.getString("password_hash"));
> 
```
</details>

<details><summary>THEIRS (preview)</summary>

```
                    String storedPasswordHash = rs.getString("password_hash");
>                     // CRITICAL: Compares the provided password (after 
```
</details>

**Bloc 15** — Type: `simple` | Résolution: ✅

**Bloc 16** — Type: `simple` | Résolution: ✅

**Bloc 17** — Type: `simple` | Résolution: ✅

**Bloc 18** — Type: `simple` | Résolution: ✅

**Bloc 19** — Type: `other` | Résolution: 🤖 LLM

<details><summary>OURS (preview)</summary>

```
            stmt.setTimestamp(5, new Timestamp(System.currentTimeMillis()));
>             stmt.setBoolean(6, true);
> 
```
</details>

<details><summary>THEIRS (preview)</summary>

```
            stmt.setTimestamp(5, new Timestamp(System.currentTimeMillis())); // Set creation timestamp              stmt.setBoolean(6, true); // Defau
```
</details>

**Bloc 20** — Type: `simple` | Résolution: ✅

**Bloc 21** — Type: `simple` | Résolution: ✅

**Bloc 22** — Type: `simple` | Résolution: ✅

**Bloc 23** — Type: `simple` | Résolution: ✅

**Bloc 24** — Type: `other` | Résolution: 🤖 LLM

<details><summary>OURS (preview)</summary>

```
        String query = "SELECT * FROM users";
> 
```
</details>

<details><summary>THEIRS (preview)</summary>

```

>         // CRITICAL: The original query only selected id, username, leading to incomplete User objects.
>         // All fields required for a complete
```
</details>

**Bloc 25** — Type: `simple` | Résolution: ✅

**Bloc 26** — Type: `simple` | Résolution: ✅

**Bloc 27** — Type: `other` | Résolution: 🤖 LLM

<details><summary>OURS (preview)</summary>

```
                users.add(mapUser(rs));
> 
```
</details>

<details><summary>THEIRS (preview)</summary>

```
                // CRITICAL: Populating User object using the parameterized constructor for completeness.
>                 int id = rs.getInt("id");
>   
```
</details>

**Bloc 28** — Type: `simple` | Résolution: ✅

**Bloc 29** — Type: `other` | Résolution: 🤖 LLM

<details><summary>OURS (preview)</summary>

```
            java.security.MessageDigest md = java.security.MessageDigest.getInstance("MD5");
>             byte[] hash = md.digest(password.getBytes());
```
</details>

<details><summary>THEIRS (preview)</summary>

```
            java.security.MessageDigest md = java.security.MessageDigest.getInstance("SHA-256");
>             byte[] hash = md.digest(password.getBytes
```
</details>

**Bloc 30** — Type: `simple` | Résolution: ✅

**Bloc 31** — Type: `method` | Résolution: 🤖 LLM

<details><summary>OURS (preview)</summary>

```
    private User mapUser(ResultSet rs) throws SQLException {
>         return new User(
>                 rs.getInt("id"), rs.getString("username"), rs.ge
```
</details>

## Instructions pour le reviewer

1. Vérifier les résolutions interactives (marquées 🔵/🟡/🤖)
2. Vérifier qu'aucune vulnérabilité n'a été réintroduite
3. Exécuter les tests unitaires avant de merger

```bash
git fetch origin && git checkout auto-resolve/pr-6
git diff main..HEAD
```

---
*Généré automatiquement par Code Auditor v7.3*