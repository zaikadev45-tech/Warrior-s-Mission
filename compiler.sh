g++ -I header -c src/Personagens.cpp -o obj/Personagens.o
g++ -I header -c src/Player.cpp -o obj/Player.o
g++ -I header -c src/Monster.cpp -o obj/Monster.o
g++ -I header -c src/Historia.cpp -o obj/Historia.o
g++ -I header -c main.cpp -o obj/main.o
g++ obj/*.o -o RPG
