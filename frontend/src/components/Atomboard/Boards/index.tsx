import { BoardContainer } from './BoardContainer.tsx';
import { RepositoryBoard } from './RepositoryBoard.tsx';
import { DeveloperBoard } from './DeveloperBoard.tsx';
import { CompanyBoard } from './CompanyBoard.tsx';
import { CountryBoard } from './CountryBoard.tsx';
import React from 'react';

export const Boards: React.FC = () => {

  return (
    <div>
      <BoardContainer top='79px' left='50px' title='高校开源贡献度排行榜'>
        <CountryBoard />
      </BoardContainer>

        <BoardContainer top='537px' left='50px' title='学生开源贡献度排行榜'>
            <RepositoryBoard />
        </BoardContainer>

        <BoardContainer  top='79px' left='1320px' title='开源项目接受贡献排行榜'>
            <DeveloperBoard  />
        </BoardContainer>
      
      <BoardContainer top='537px' left='1320px' title='企业接受开源贡献排行榜'>

          <CompanyBoard />
      </BoardContainer>
    </div >
  )
}
