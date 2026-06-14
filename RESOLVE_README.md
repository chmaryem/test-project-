# Auto-Resolve Report — PR #17

> **PR originale** : #17 — *change on feature*
> **Branche base** : `main` | **Branche source** : `feature/test-hook`
> **Généré par** : Code Auditor v7.3 (Interactive + RAG-enhanced resolution)

## Résumé des résolutions

| Fichier | Méthode | Conflits | Détails |
|---|---|---|---|
| `src/main/java/tn/esprit/sampleprojet/UserService.java` | `fallback` | 29 | 18 auto, 11 interactif |

## Détails des résolutions

### `UserService.java`

**Bloc 1** — Type: `other` | Résolution: 🤖 LLM

<details><summary>THEIRS (preview)</summary>

```
package tn.esprit.sampleprojet;
> 
> import org.springframework.beans.factory.annotation.Autowired;
> import org.springframework.stereotype.Service;
> import 
```
</details>

**Bloc 2** — Type: `simple` | Résolution: ✅

**Bloc 3** — Type: `simple` | Résolution: ✅

**Bloc 4** — Type: `simple` | Résolution: ✅

**Bloc 5** — Type: `other` | Résolution: 🤖 LLM

<details><summary>OURS (preview)</summary>

```
        Connection conn = dataSource.getConnection();
>         PreparedStatement stmt = conn.prepareStatement(query);
> 
```
</details>

<details><summary>THEIRS (preview)</summary>

```
                // All fields required for a complete User object (as per User constructor) should be retrieved.
>                 String query = "SELEC
```
</details>

**Bloc 6** — Type: `other` | Résolution: 🤖 LLM

<details><summary>OURS (preview)</summary>

```
        // ❌ oubli du setString → bug silencieux
>         ResultSet rs = stmt.executeQuery();
> 
```
</details>

<details><summary>THEIRS (preview)</summary>

```
        try (Connection conn = dataSource.getConnection();
>              PreparedStatement stmt = conn.prepareStatement(query)) {
> 
```
</details>

**Bloc 7** — Type: `other` | Résolution: 🤖 LLM

<details><summary>OURS (preview)</summary>

```
        if (rs.next()) {
>             User user = new User();
>             user.id = rs.getInt("id");
>             user.username = rs.getString("username
```
</details>

<details><summary>THEIRS (preview)</summary>

```
            stmt.setString(1, username);
>             try (ResultSet rs = stmt.executeQuery()) {
> 
>                 if (rs.next()) {
> 
>                    
```
</details>

**Bloc 8** — Type: `simple` | Résolution: ✅

**Bloc 9** — Type: `simple` | Résolution: ✅

**Bloc 10** — Type: `simple` | Résolution: ✅

**Bloc 11** — Type: `other` | Résolution: 🤖 LLM

<details><summary>OURS (preview)</summary>

```
            if (rs.next()) {
>                 String storedPassword = rs.getString("password_hash");
> 
>                 // ❌ comparaison incorrecte (== a
```
</details>

<details><summary>THEIRS (preview)</summary>

```
                if (rs.next()) {
>                     String storedPasswordHash = rs.getString("password_hash");
>                     // CRITICAL: Compa
```
</details>

**Bloc 12** — Type: `simple` | Résolution: ✅

**Bloc 13** — Type: `simple` | Résolution: ✅

**Bloc 14** — Type: `simple` | Résolution: ✅

**Bloc 15** — Type: `simple` | Résolution: ✅

**Bloc 16** — Type: `simple` | Résolution: ✅

**Bloc 17** — Type: `other` | Résolution: 🤖 LLM

<details><summary>OURS (preview)</summary>

```
        // ❌ retourne un objet incomplet
>         User u = new User();
>         u.username = username;
>         return u;
> 
```
</details>

<details><summary>THEIRS (preview)</summary>

```
        return findByUsername(username);
> 
```
</details>

**Bloc 18** — Type: `simple` | Résolution: ✅

**Bloc 19** — Type: `other` | Résolution: 🤖 LLM

<details><summary>OURS (preview)</summary>

```
            stmt.setInt(1, userId);     // ❌ inversion des paramètres
>             stmt.setBoolean(2, isActive);
> 
```
</details>

<details><summary>THEIRS (preview)</summary>

```
            stmt.setBoolean(1, isActive);
>             stmt.setInt(2, userId);
> 
```
</details>

**Bloc 20** — Type: `simple` | Résolution: ✅

**Bloc 21** — Type: `other` | Résolution: 🤖 LLM

<details><summary>OURS (preview)</summary>

```
        String query = "SELECT id, username FROM users";
> 
```
</details>

<details><summary>THEIRS (preview)</summary>

```
        // CRITICAL: The original query only selected id, username, leading to incomplete User objects.
>         // All fields required for a complete 
```
</details>

**Bloc 22** — Type: `simple` | Résolution: ✅

**Bloc 23** — Type: `other` | Résolution: 🤖 LLM

<details><summary>OURS (preview)</summary>

```
                User user = new User();
>                 user.id = rs.getInt("id");
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

**Bloc 24** — Type: `other` | Résolution: 🤖 LLM

<details><summary>OURS (preview)</summary>

```
                // ❌ erreur logique: mauvaise colonne
>                 user.username = rs.getString("email");
> 
```
</details>

<details><summary>THEIRS (preview)</summary>

```
                Date createdAt = (createdAtTimestamp != null) ? new Date(createdAtTimestamp.getTime()) : null;
>                 Date lastLogin = (lastL
```
</details>

**Bloc 25** — Type: `simple` | Résolution: ✅

**Bloc 26** — Type: `simple` | Résolution: ✅

**Bloc 27** — Type: `simple` | Résolution: ✅

**Bloc 28** — Type: `other` | Résolution: 🤖 LLM

<details><summary>OURS (preview)</summary>

```
        // ❌ hash faible + bug logique
>         return password.toLowerCase().trim(); // pas un vrai hash
> 
```
</details>

<details><summary>THEIRS (preview)</summary>

```
        try {
>             java.security.MessageDigest md = java.security.MessageDigest.getInstance("SHA-256");
>             byte[] hash = md.digest(pas
```
</details>

**Bloc 29** — Type: `simple` | Résolution: ✅

## Instructions pour le reviewer

1. Vérifier les résolutions interactives (marquées 🔵/🟡/🤖)
2. Vérifier qu'aucune vulnérabilité n'a été réintroduite
3. Exécuter les tests unitaires avant de merger

```bash
git fetch origin && git checkout auto-resolve/pr-17
git diff main..HEAD
```

---
*Généré automatiquement par Code Auditor v7.3*