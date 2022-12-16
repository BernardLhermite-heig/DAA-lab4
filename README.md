# Développement Android

## Laboratoire n°4: Tâches asynchrones et Coroutines - Galerie d'images

### Friedli Jonathan, Marengo Stéphane, Silvestri Géraud

### 09.12.2022

## Introduction

Le but de ce laboratoire est de développer une application android faisant office de galerie d'images. L'application devra permettre de scroller à travers une liste de 10'000 images, qui seront téléchargées en ligne. Le but de ce laboratoire est de se familiariser avec les tâches asynchrones (`coroutines` et `WorkManager`).

<div style="page-break-after: always;"></div>

## 1. Détails d'implémentation

### 1.1. Layout


## 2. Questions sur l'Adapteur et les coroutines

### 2.1 

**Veuillez expliquer comment votre solution s'assure qu'une éventuelle Couroutine associée à une vue (item) de la RecyclerView soit correctement stoppée lorsque l'utilisateur scrolle dans la galerie et que la vue est recyclée.**

### 2.2

**Comment pouvons-nous nous assurer que toutes les Coroutines soient correctement stoppées lorsque l'utilisateur quitte l'Activité ? Veuillez expliquer la solution que vous avez mis en oeuvre, est-ce la plus adaptée ?**

### 2.3 
**Est-ce que l'utilisation du Dispatchers.IO est le plus adapté pour des tâches de téléchargement ? Ne faudrait-il pas plutôt utiliser un autre Dispatcher, si oui lequel ? Veuillez illustrer votre réponse en effectuant quelques tests.**

## 3. Questions sur le nettoyage automatique du cache

### 3.1

**Lors du lancement de la tâche ponctuelle, comment pouvons nous faire en sorte que la galerie soit raffraîchie ?**

### 3.2 

**Comment pouvons-nous nous assurer que la tâche périodique ne soit pas enregistrée plusieurs fois ? Vous expliquerez comment la librairie WorkManager procède pour enregistrer les différentes tâches périodiques et en particulier comment celles-ci sont ré-enregistrées lorsque le téléphone est redémarré.**