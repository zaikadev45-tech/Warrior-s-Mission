g++ -I header -c src/Personagens.cpp -o obj/Personagens.o
echo "codigo de saida: $?, parte 1 completo";
g++ -I header -c src/Player.cpp -o obj/Player.o
echo "codigo de saida: $?, parte 2 completo";
g++ -I header -c src/Monster.cpp -o obj/Monster.o
echo "codigo de saida: $?, parte 3 completo";
g++ -I header -c src/Historia.cpp -o obj/Historia.o
echo "codigo de saida: $?, parte 4 completo";
g++ -I header -c main.cpp -o obj/main.o
echo "codigo de saida: $?, parte 5 completo";
g++ obj/*.o -o RPG
echo "RPG CRIADO COM SUCESSO";
