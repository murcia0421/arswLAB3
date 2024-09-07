# Laboratorio 3 - Previniendo abrazos mortales (DEAD LOCKS)

## Parte I: Control de hilos con wait/notify - Productor/consumidor

### 1. Análisis del funcionamiento
Ejecute el programa y observe el comportamiento del consumo de CPU utilizando jVisualVM. Notará que el consumo es moderadamente alto. Las clases responsables de este comportamiento son `Producer` y `Consumer`:

- **Producer**: Ejecuta un ciclo infinito en el que no deja de producir, lo que puede causar un alto uso de la CPU.
- **Consumer**: También consume mucha CPU, ya que no tiene una manera eficiente de verificar cuándo la cola está vacía y sigue buscando elementos en un ciclo infinito (espera activa).


![image](https://github.com/user-attachments/assets/f1cd98f7-252e-48db-805b-86966e6082d6)

### 2. Ajustes para mejorar el uso de la CPU
Se realizaron los siguientes cambios para mejorar el rendimiento y reducir el consumo de CPU:

- El **consumidor** ahora utiliza el método `queue.take()`, que bloquea el hilo hasta que haya un elemento disponible en la cola, evitando que el consumidor esté constantemente revisando la cola.
![image](https://github.com/user-attachments/assets/b2a34920-565d-43c1-a489-b5bff7e03967)

  
- El **productor** tiene ahora una cantidad límite de producción, lo que le permite detenerse una vez alcanzado ese límite, controlando el uso de la CPU.

![image](https://github.com/user-attachments/assets/d41dae87-17af-4a76-a843-2fba26aab9e2)


Después de estos ajustes, el consumo de CPU se redujo prácticamente a cero, lo que indica un mejor rendimiento del programa.

![image](https://github.com/user-attachments/assets/de977032-c781-4a04-b6fe-133fc5d48f28)


### 3. Productor rápido y consumidor lento
Se cambió la configuración del programa para que el **productor** produzca rápidamente y el **consumidor** consuma lentamente. Se implementaron las siguientes modificaciones:

- El productor tiene un límite de "stock", es decir, cuántos elementos como máximo debería tener la cola.
- Se agregó una pausa (`.sleep`) al consumidor para reducir su velocidad de consumo. De esta manera, se permite que el productor sea muy rápido y el consumidor lento, sin afectar negativamente el rendimiento del sistema ni incrementar el consumo de CPU.

## Parte II: Búsqueda distribuida y sincronización

### 1. Optimización del buscador de listas negras
Se mejoró la eficiencia de la búsqueda distribuida en listas negras. En la versión actual del programa, cada hilo se encarga de revisar una parte de los servidores. La optimización consistió en que la búsqueda se detenga tan pronto como se detecte el número de ocurrencias requeridas para determinar si un host es confiable o no (definido por la constante `BLACK_LIST_ALARM_COUNT`).

- Cada hilo verifica si ya se han encontrado las ocurrencias necesarias (5 en este caso) y, de ser así, se detiene para evitar continuar revisando innecesariamente.
![image](https://github.com/user-attachments/assets/1a8d3e56-cedc-4ba4-a0f4-d98429116b6b)



### 2. Prevención de condiciones de carrera
Se implementaron métodos `synchronized` para evitar condiciones de carrera, asegurando que solo un hilo a la vez pueda actualizar el número total de ocurrencias y la lista de las listas negras donde se encontró la IP. Los hilos verifican el valor de retorno durante su ejecución, y si reciben `true`, terminan su búsqueda.

## Parte III: Sincronización y Dead-Locks

![image](https://github.com/user-attachments/assets/f3584bf7-a350-4142-b678-9a45f86e175a)


### Revisión del simulador de Highlanders
Este simulador contiene N jugadores inmortales que pelean entre sí. Cada jugador:

1. Conoce a los N-1 jugadores restantes.
2. Ataca a otro inmortal, restándole M puntos de vida y sumando esa cantidad a su propia vida.

### Invariante del sistema
Un invariante importante del sistema es que la sumatoria de los puntos de vida de todos los jugadores debe permanecer constante (excepto durante las operaciones de incremento/reducción de vida en tiempo real). Si hay N jugadores, la suma total de sus puntos de vida debería ser:

\[
\text{Invariante} = N \times 100
\]

Al ejecutar la aplicación, se verificó que no se cumple el invariante. Por ejemplo, con 3 inmortales, la suma de las vidas es 310, cuando debería ser 300.

### Pausa y verificación
Se identificó que la causa de esta inconsistencia es una condición de carrera. Para solucionarlo, se pausaron todos los hilos antes de imprimir los resultados y se implementó una opción `resume` para reanudar su ejecución.

### Bloqueo de peleas
Se identificaron regiones críticas en el código durante la pelea de los inmortales. Para evitar condiciones de carrera, se implementó un bloqueo de ambos inmortales (el atacante y el defensor) durante la operación de pelea.
![image](https://github.com/user-attachments/assets/34c17428-9287-44bf-ba0f-effe691a4372)



### Detección de deadlocks
Tras ejecutar el programa, se encontró un deadlock entre dos inmortales. Usando `jps` y `jstack`, se identificó que el inmortal 1 y el inmortal 2 se estaban bloqueando mutuamente. La estrategia para corregir este problema fue asegurarse de que los hilos siempre se bloqueen en un orden consistente para evitar deadlocks.
![image](https://github.com/user-attachments/assets/559eabfc-85c9-45c2-8496-8709d9fd8b90)

![image](https://github.com/user-attachments/assets/bc29dba1-2999-45bf-be5f-59b8db40fa95)


### Eliminación de inmortales muertos
Otro problema identificado es que los inmortales muertos seguían siendo atacados, lo que generaba peleas fallidas. Para solucionarlo:

1. Se implementó la eliminación de inmortales muertos de la simulación.
2. Se comprobó que esta solución no crea condiciones de carrera ni ralentiza la simulación.

![image](https://github.com/user-attachments/assets/c110d446-0967-47b4-9136-710909d4b21e)



### Pruebas de carga
La aplicación fue probada con diferentes números de hilos (100, 1000 y 10,000), y se verificó que el programa sigue funcionando correctamente sin incumplir el invariante.

![image](https://github.com/user-attachments/assets/b779a5ae-3708-49cf-a7b4-b4ec31d8df6f)

![image](https://github.com/user-attachments/assets/28ac77aa-ed82-49b9-a84d-cd60a1304c4b)


### Implementación del botón STOP
Se implementó un botón `STOP` que detiene la ejecución de todos los hilos correctamente.
