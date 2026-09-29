# 📦 Guide d'installation — Bibliothèque Municipale

## Prérequis

- Serveur Linux (ou compte Render.com)
- Base de données PostgreSQL 14+
- Java 21 (fourni par Docker)
- Nom de domaine (optionnel)

## Déploiement sur Render (recommandé)

1. Créer un compte sur https://render.com
2. Créer une base PostgreSQL (plan gratuit)
3. Créer un Web Service connecté au repo GitHub
4. Ajouter 3 variables d'environnement :
   - `SPRING_DATASOURCE_URL`
   - `SPRING_DATASOURCE_USERNAME`
   - `SPRING_DATASOURCE_PASSWORD`
5. Déployer

## Coût mensuel

- Render Web Service (Free) : 0€ (avec sleep 15min)
- Render Web Service (Starter) : 7€/mois (pas de sleep)
- Base PostgreSQL Free : 0€ (1 GB)
- Nom de domaine : ~10€/an